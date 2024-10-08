package nablarch.core.transaction;

import nablarch.test.support.reflection.ReflectionUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;


public class TransactionContextTest {

    Transaction mockTransaction1 = mock(Transaction.class);

    Transaction mockTransaction2 = mock(Transaction.class);

    @Before
    public void setUp() throws Exception {
        final ThreadLocal<Map<String, Transaction>> transaction = ReflectionUtil.getFieldValue(TransactionContext.class, "transaction");
        transaction.remove();
        TransactionContext.removeTransaction();
    }

    @After
    public void tearDown() throws Exception {
        final ThreadLocal<Map<String, Transaction>> transaction = ReflectionUtil.getFieldValue(TransactionContext.class, "transaction");
        transaction.remove();
        TransactionContext.removeTransaction();
    }

    @Test
    public void setTransaction() {
        TransactionContext
                .setTransaction(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY, mockTransaction1);

        // 設定したものと同一のオブジェクトが取得できること。
        assertThat(TransactionContext.getTransaction(), is(mockTransaction1));

        // 別名では登録できること。
        TransactionContext.setTransaction("testTran", mockTransaction2);
        assertThat(TransactionContext.getTransaction("testTran"), is(mockTransaction2));

        // 削除
        TransactionContext.removeTransaction("testTran");

        // 削除後の取得は、エラーとなる。
        try {
            TransactionContext.getTransaction("testTran");
            fail("does not run.");
        } catch (Exception e) {
            assertThat(e.getMessage(),
                    is("specified transaction name is not register in thread local. transaction name = [testTran]"));
        }

        // 同一の名前で登録した場合は、エラーとなる。
        try {
            TransactionContext
                    .setTransaction(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY, mockTransaction2);
            fail("does not run.");
        } catch (Exception e) {
            assertThat(e.getMessage(), is(String.format(
                    "specified transaction name was duplication in thread local. transaction name = [%s]",
                    TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY)));
        }

        // 重複登録でエラーとなった場合に、エラー発生前のトランザクションが取得できること。
        assertThat(TransactionContext.getTransaction(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY),
                is(mockTransaction1));

        // 不要となったトランザクションを削除
        TransactionContext.removeTransaction();

        // 削除されていることを確認
        try {
            TransactionContext.getTransaction();
            fail("does not run.");
        } catch (Exception e) {
            assertThat(e.getMessage(), is(String
                    .format("specified transaction name is not register in thread local. transaction name = [%s]",
                            TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY)));
        }
    }

    /**
     * {@link TransactionContext#containTransaction(String)}のテスト。
     */
    @Test
    public void testContainTransaction() throws Exception {
        assertThat("何も設定していないのでfalse",
                TransactionContext.containTransaction(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY), is(false));

        TransactionContext.setTransaction(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY, mockTransaction1);

        assertThat("設定したのでtrue",
                TransactionContext.containTransaction(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY), is(true));

        assertThat("設定されていないのでfalse", TransactionContext.containTransaction("tran"), is(false));

        TransactionContext.setTransaction("tran", mockTransaction2);
        assertThat("設定されたのでtrue", TransactionContext.containTransaction("tran"), is(true));

        TransactionContext.removeTransaction();

        assertThat("削除されたのでfalse",
                TransactionContext.containTransaction(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY), is(false));
    }

    @Test
    public void testRemoveAndReset() throws Exception {
        TransactionContext.setTransaction("test-tran", mockTransaction1);
        TransactionContext.removeTransaction("test-tran");

        try {
            TransactionContext.getTransaction("test-tran");
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        TransactionContext.setTransaction(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY, mockTransaction2);
        assertThat(TransactionContext.getTransaction(), is(sameInstance(mockTransaction2)));

        TransactionContext.removeTransaction();
    }
}
