package hc.common.constants;

public class MqConstants {
    /**
     * 交换机
     */
    public static final String TIMEBOOK_EXCHANGE="timebook.topic";
    /**
     * 监听新增和修改的队列
     */
    public static final String TIMEBOOK_INSERT_QUEUE="timebook.insert.queue";
    /**
     * 修改队列
     */
    public static final String TIMEBOOK_UPDATE_QUEUE="timebook.update.queue";
    /**
     * 监听删除的队列
     */
    public static final String TIMEBOOK_DELETE_QUEUE="timebook.delete.queue";
    /**
     * 新增的RoutingKey
     */
    public static final String TIMEBOOK_INSERT_KEY="timebook.insert";
    /**
     * 修改队列的RoutingKey
     */
    public static final String TIMEBOOK_UPDATE_KEY="timebook.update";
    /**
     * 删除的RoutingKey
     */
    public static final String TIMEBOOK_DELETE_KEY="timebook.delete";

}
