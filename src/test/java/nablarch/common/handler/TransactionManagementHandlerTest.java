package nablarch.common.handler;

import nablarch.core.transaction.Transaction;
import nablarch.core.transaction.TransactionFactory;
import nablarch.fw.ExecutionContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransactionManagementHandlerTest {

    private TransactionManagementHandler target = new TransactionManagementHandler();
    
    private final TransactionFactory transactionFactory = mock(TransactionFactory.class);

    private final Transaction transaction = mock(Transaction.class);

    private final ExecutionContext context = mock(ExecutionContext.class);

    @Before
    public void setup() {
        target.setTransactionName("tran");
        target.setTransactionFactory(transactionFactory);
        
        context.setProcessSucceeded(true);
    }
    @Test
    public void test() {
        {
            // 正常系
            when(transactionFactory.getTransaction("tran")).thenReturn(transaction);
            when(context.isProcessSucceeded()).thenReturn(true);
            
            Assert.assertThat(target.handleInbound(context)
                                    .isSuccess(), is(true));

            verify(transactionFactory).getTransaction("tran");
            verify(transaction).begin();

            Assert.assertThat(target.handleOutbound(context)
                                    .isSuccess(), is(true));
            
            verify(transaction).commit();
            verify(transaction, never()).rollback();
        }
        {
            reset(transactionFactory);
            reset(context);
            reset(transaction);
            
            // 異常系
            when(transactionFactory.getTransaction("tran")).thenReturn(transaction);
            when(context.isProcessSucceeded()).thenReturn(false);
            
            Assert.assertThat(target.handleInbound(context)
                                    .isSuccess(), is(true));

            verify(transactionFactory).getTransaction("tran");
            verify(transaction).begin();

            Assert.assertThat(target.handleOutbound(context)
                                    .isSuccess(), is(true));

            verify(transaction, never()).commit();
            verify(transaction).rollback();
        }
        {
            reset(transactionFactory);
            reset(context);
            reset(transaction);
            
            when(transactionFactory.getTransaction("tran")).thenReturn(transaction);

            // トランザクション開始前に Outbound が呼ばれた場合
            Assert.assertThat(target.handleOutbound(context)
                                    .isSuccess(), is(true));

            verify(transaction, never()).commit();
            verify(transaction, never()).rollback();
        }
    }

}
