package com.chinawinddata.hbase.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@DependsOn("springContextHolder")		//控制依赖顺序，保证springContextHolder类在之前已经加载
@Component
public class HBaseUtils {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
	
    private static Environment environment;

    private static Configuration conf = HBaseConfiguration.create();
    private static ExecutorService pool = Executors.newScheduledThreadPool(20);	//设置连接池
    private static Connection connection = null;
    private static HBaseUtils instance = null;
    private static Admin admin = null;

    @Autowired
    private HBaseUtils(Environment env) {
    	HBaseUtils.environment = env;
        if(connection == null){
            try {
            	//将hbase配置类中定义的配置加载到连接池中每个连接里
            	String hbaseQuorum = environment.getProperty("hbase.conf.hbase.zookeeper.quorum");
            	conf.set("hbase.zookeeper.quorum", hbaseQuorum);
                connection = ConnectionFactory.createConnection(conf, pool);
                admin = connection.getAdmin();
            } catch (IOException e) {
                logger.error("HbaseUtils实例初始化失败！错误信息为：" + e.getMessage(), e);
            }
        }
    }
	
	//简单单例方法，如果autowired自动注入就不需要此方法
    public static synchronized HBaseUtils getInstance(){
        if(instance == null){
            //instance = new HBaseUtils();
        }
        return instance;
    }


    /**
     * 创建表
     *
     * @param tableName         表名
     * @param columnFamily      列族（数组）
     */
    public void createTable(String tableName, String[] columnFamily) throws IOException{
        TableName name = TableName.valueOf(tableName);
        //如果存在则删除
        if (admin.tableExists(name)) {
            admin.disableTable(name);
            admin.deleteTable(name);
            logger.error("create htable error! this table {} already exists!", name);
        } else {
            HTableDescriptor desc = new HTableDescriptor(name);
            for (String cf : columnFamily) {
                desc.addFamily(new HColumnDescriptor(cf));
            }
            admin.createTable(desc);
        }
    }

    /**
     * 插入记录（单行单列族-多列多值）
     *
     * @param tableName         表名
     * @param row               行名
     * @param columnFamilys     列族名
     * @param columns           列名（数组）
     * @param values            值（数组）（且需要和列一一对应）
     */
    public void insertRecords(String tableName, String row, String columnFamilys, String[] columns, String[] values) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = connection.getTable(name);
        Put put = new Put(Bytes.toBytes(row));
        for (int i = 0; i < columns.length; i++) {
            put.addColumn(Bytes.toBytes(columnFamilys), Bytes.toBytes(columns[i]), Bytes.toBytes(values[i]));
            table.put(put);
        }
    }

    /**
     * 插入记录（单行单列族-单列单值）
     *
     * @param tableName         表名
     * @param row               行名
     * @param columnFamily      列族名
     * @param column            列名
     * @param value             值
     */
    public void insertOneRecord(String tableName, String row, String columnFamily, String column, String value) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = connection.getTable(name);
        Put put = new Put(Bytes.toBytes(row));
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column), Bytes.toBytes(value));
        table.put(put);
    }

    /**
     * 删除一行记录
     *
     * @param tablename         表名
     * @param rowkey            行名
     */
    public void deleteRow(String tableName, String rowKey) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = connection.getTable(name);
        Delete d = new Delete(rowKey.getBytes());
        table.delete(d);
    }

    /**
     * 删除单行单列族记录
     * @param tablename         表名
     * @param rowkey            行名
     * @param columnFamily      列族名
     */
    public void deleteColumnFamily(String tableName, String rowkey, String columnFamily) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = connection.getTable(name);
        Delete d = new Delete(rowkey.getBytes()).deleteFamily(Bytes.toBytes(columnFamily));
        table.delete(d);
    }

    /**
     * 删除单行单列族单列记录
     *
     * @param tablename         表名
     * @param rowkey            行名
     * @param columnFamily      列族名
     * @param column            列名
     */
    public void deleteColumn(String tableName, String rowkey, String columnFamily, String column) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = connection.getTable(name);
        Delete d = new Delete(rowkey.getBytes()).deleteColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column));
        table.delete(d);
    }


    /**
     * 查找一行记录
     *
     * @param tablename         表名
     * @param rowKey            行名
     */
    public static String selectRow(String tableName, String rowKey) throws IOException {
        String record = "";
        TableName name=TableName.valueOf(tableName);
        Table table = connection.getTable(name);
        Get g = new Get(rowKey.getBytes());
        Result rs = table.get(g);
        NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs.getMap();
        for (Cell cell : rs.rawCells()) {
            StringBuffer stringBuffer = new StringBuffer().append(Bytes.toString(cell.getRow())).append("\t")
                    .append(Bytes.toString(cell.getFamily())).append("\t")
                    .append(Bytes.toString(cell.getQualifier())).append("\t")
                    .append(Bytes.toString(cell.getValue())).append("\n");
            String str = stringBuffer.toString();
            record += str;
        }
        return record;
    }

    /**
     * 查找单行单列族单列记录
     *
     * @param tablename         表名
     * @param rowKey            行名
     * @param columnFamily      列族名
     * @param column            列名
     * @return
     */
    public static String selectValue(String tableName, String rowKey, String columnFamily, String column) throws IOException {
        TableName name=TableName.valueOf(tableName);
        Table table = connection.getTable(name);
        Get g = new Get(rowKey.getBytes());
        g.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column));
        Result rs = table.get(g);
        return Bytes.toString(rs.value());
    }

    /**
     * 查询表中所有行（Scan方式）
     *
     * @param tablename
     * @return
     */
    public String scanByTableName(String tableName) throws IOException {
        String record = "";
        TableName name=TableName.valueOf(tableName);
        Table table = connection.getTable(name);
        Scan scan = new Scan();
        ResultScanner scanner = table.getScanner(scan);
        try {
            for(Result result : scanner){
                for (Cell cell : result.rawCells()) {
                    StringBuffer stringBuffer = new StringBuffer().append(Bytes.toString(cell.getRow())).append("\t")
                            .append(Bytes.toString(cell.getFamily())).append("\t")
                            .append(Bytes.toString(cell.getQualifier())).append("\t")
                            .append(Bytes.toString(cell.getValue())).append("\n");
                    String str = stringBuffer.toString();
                    record += str;
                }
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return record;
    }
    
    public List<Result> scanByStartEnd(String tableName, String start, String end) throws IOException {
        List<Result> scanResult = new ArrayList<Result>();
        TableName name=TableName.valueOf(tableName);
        Table table = connection.getTable(name);
        Scan scan = new Scan(start.getBytes(), end.getBytes());
        ResultScanner resultScanner = table.getScanner(scan);
        Iterator<Result> iterator = resultScanner.iterator();
        while (iterator.hasNext()) {
            Result result = iterator.next();
            scanResult.add(result);
        }
        return scanResult;
    }

    /**
     * 根据rowkey关键字查询报告记录
     *
     * @param tablename
     * @param rowKeyword
     * @return
     */
    public List<Object> scanByRowKeyword(String tableName, String rowKeyword) throws IOException {
        ArrayList<Object> list = new ArrayList<Object>();

        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
		
		//添加行键过滤器，根据关键字匹配
        RowFilter rowFilter = new RowFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator(rowKeyword));
        scan.setFilter(rowFilter);

        ResultScanner scanner = table.getScanner(scan);
        try {
            for (Result result : scanner) {
            	//TODO 此处根据业务来自定义实现
            	list.add(result);
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return list;
    }

    /**
     * 根据rowkey关键字和时间戳范围查询报告记录
     *
     * @param tablename
     * @param rowKeyword
     * @return
     */
    public List<Object> scanByRowKeywordTimestamp(String tableName, String rowKeyword, Long minStamp, Long maxStamp) throws IOException {
        ArrayList<Object> list = new ArrayList<Object>();

        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        //添加scan的时间范围
        scan.setTimeRange(minStamp, maxStamp);

        RowFilter rowFilter = new RowFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator(rowKeyword));
        scan.setFilter(rowFilter);

        ResultScanner scanner = table.getScanner(scan);
        try {
            for (Result result : scanner) {
                //TODO 此处根据业务来自定义实现
                list.add(result);
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return list;
    }

    /**
     * 删除表操作
     *
     * @param tablename
     */
    public void deleteTable(String tableName) throws IOException {
        TableName name=TableName.valueOf(tableName);
        if(admin.tableExists(name)) {
            admin.disableTable(name);
            admin.deleteTable(name);
        }
    }

    /**
     * 利用协处理器进行全表count统计
     *
     * @param tablename
     */
    public Long countRowsWithCoprocessor(String tableName) throws Throwable {
        TableName name=TableName.valueOf(tableName);
        HTableDescriptor descriptor = admin.getTableDescriptor(name);

        String coprocessorClass = "org.apache.hadoop.hbase.coprocessor.AggregateImplementation";
        if (! descriptor.hasCoprocessor(coprocessorClass)) {
            admin.disableTable(name);
            descriptor.addCoprocessor(coprocessorClass);
            admin.modifyTable(name, descriptor);
            admin.enableTable(name);
        }

        //计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Scan scan = new Scan();
        AggregationClient aggregationClient = new AggregationClient(conf);

        Long count = aggregationClient.rowCount(name, new LongColumnInterpreter(), scan);

        stopWatch.stop();
        System.out.println("RowCount：" + count +  "，全表count统计耗时：" + stopWatch.getTotalTimeMillis());

        return count;
    }

}