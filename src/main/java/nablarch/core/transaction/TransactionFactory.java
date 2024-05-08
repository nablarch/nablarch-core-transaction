package nablarch.core.transaction;

import nablarch.core.util.annotation.Published;

/**
 * トランザクション制御オブジェクト({@link Transaction})を生成するインタフェース。。
 *
 * @author Hisaaki Sioiri
 */
@Published(tag = "architect")
public interface TransactionFactory {

    /**
     * トランザクションオブジェクトを生成する。
     *
     * @param resourceName リソース名
     * @return トランザクションオブジェクト
     */
    Transaction getTransaction(String resourceName);

}
