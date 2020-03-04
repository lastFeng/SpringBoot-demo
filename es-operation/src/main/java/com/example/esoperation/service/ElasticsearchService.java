/*
 * Copyright 2001-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.esoperation.service;

import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.HasAggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.adjacency.ParsedAdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.significant.ParsedSignificantStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.ParsedCardinality;
import org.elasticsearch.search.aggregations.metrics.stats.ParsedStats;
import org.elasticsearch.search.aggregations.metrics.stats.StatsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p> Title: </p>
 *
 * <p> Description: </p>
 *
 * @author: Guo Weifeng
 * @version: 1.0
 * @create: 2020/3/3 10:05
 */
@Service
public class ElasticsearchService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);

    private static final int TOP_MAX_VALUE = 10;

    private static final String AGGREGATE_TYPE = "type";

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 创建索引
     * @param index
     */
    public void createIndex(String index) {
        CreateIndexRequest request = new CreateIndexRequest(index);
        try {
            restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            logger.error("ES Create Index failed! ", e.getMessage());
        }
    }

    /**
     * 查看索引是否存在
     * @param index
     * @return
     */
    public boolean isExistIndex(String index) {
        GetIndexRequest request = new GetIndexRequest();
        request.indices(index);

        try {
            boolean isExisted = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
            return isExisted;
        } catch (Exception e) {
            logger.error("ES index search failed! ", e.getMessage());
        }

        return false;
    }

    /**
     * 新增单个数据
     * @param index
     * @param type
     * @param id
     * @param jsonData
     * @return
     */
    public boolean addData(String index, String type, String id, String jsonData) {
        UpdateRequest request = new UpdateRequest(index, type, id);
        request.doc(jsonData, XContentType.JSON);
        request.upsert(jsonData, XContentType.JSON);

        // 设置超时时间、刷新策略已经重试次数
        request.timeout(TimeValue.timeValueMinutes(1));
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        request.retryOnConflict(3);

        try {
            UpdateResponse response = restHighLevelClient.update(request, RequestOptions.DEFAULT);
            // 创建成功
            if (response.getResult() == DocWriteResponse.Result.CREATED) {
                // record the log of created
            } else if (response.getResult() == DocWriteResponse.Result.UPDATED) {
                // record the log of updated
            }

            ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
            // 新增失败时的错误
            if (shardInfo.getFailed() > 0){
                // log the failures reason.
                return false;
            }
            return true;
        } catch (IOException e) {
            logger.error("ES addData failed! ", e.getMessage());
        }
        return false;
    }

    /**
     * 删除指定数据
     * @param index
     * @param type
     * @param id
     * @return
     */
    public boolean deleteData(String index, String type, String id) {
        DeleteRequest request = new DeleteRequest(index, type, id);

        // 对删除操作设置超时时间、刷新策略
        request.timeout(TimeValue.timeValueMinutes(1));
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        try {
            DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            if (response.getResult() == DocWriteResponse.Result.NOT_FOUND) {
                // record the log
                return true;
            }

            if (response.getShardInfo().getFailed() > 0) {
                // record the log
                return false;
            }
        } catch (IOException e) {
            logger.error("ES delete data failed! ", e.getMessage());
        }
        return false;
    }

    /**
     * 模糊过滤技术查询
     * @param indices
     * @param filterParams 每一个map元素都是一个过滤条件
     *                     其中Map.key时属性名称，Map.value时属性值数组，多个属性值数组是或（or）的关系
     *                     过个map元素之间是与（and）的关系
     * @param timeRange 时间范围支持的时间格式：
     *                  1. quick时间表达式(now - 1d, now)
     *                  2. 带时区的时间格式(2020-0303T11:58:30.000+0800)
     *                  3. 不带时区的时间格式：yyyy-MM-dd HH:mm:ss
     *                  本例格式均一致
     * @return
     */
    public long count(String[] indices, Map<String, Object[]> filterParams,
                      Map<String, String[]> timeRange) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 只返回聚集结果，不返回文档数据
        sourceBuilder.size(0);

        // 设置过滤条件, bool过滤
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 时间范围过滤
        setTimeRange(boolQueryBuilder, timeRange);

        // 属性过滤
        setFilterParams(boolQueryBuilder, filterParams);
        sourceBuilder.query(boolQueryBuilder);

        // 搜索
        SearchRequest request = new SearchRequest(indices);
        // 设置搜索条件
        request.source(sourceBuilder);

        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            long result = response.getHits().getTotalHits();
            return result;
        } catch (IOException e) {
            logger.error("ES search failed! ", e.getMessage());
        }
        return 0L;
    }

    /**
     * 对事件指定属性进行去重统计
     * @param indices
     * @param disField : 聚合名称以及需要查询的指定属性
     * @param filterParams
     * @param timeRange
     * @return
     */
    public long disCount(String[] indices, String disField,
                         Map<String, Object[]> filterParams, Map<String, String[]> timeRange) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 只返回聚集结果
        sourceBuilder.size(0);

        // 生成聚合语句
        if (StringUtils.isNotEmpty(disField)) {
            CardinalityAggregationBuilder aggregationBuilder = AggregationBuilders
                // 聚合名称
                .cardinality(disField)
                // 指定聚合属性
                .field(disField);
            sourceBuilder.aggregation(aggregationBuilder);
        } else {
            return 0L;
        }

        // 设置过滤条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        setTimeRange(boolQueryBuilder, timeRange);
        setFilterParams(boolQueryBuilder, filterParams);
        sourceBuilder.query(boolQueryBuilder);

        SearchRequest request = new SearchRequest(indices);
        request.source(sourceBuilder);

        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            // 获得聚合结果
            Aggregations aggregations = response.getAggregations();
            if (aggregations != null) {
                ParsedCardinality cardinality = aggregations.get(disField);
                return cardinality.getValue();
            }
        } catch (IOException e) {
            logger.error("Aggregations failed! ", e.getMessage());
        }
        return 0L;
    }

    /**
     * 根据属性进行分组计数统计，支持二维统计
     * @param indices 索引列表
     * @param aggField 一维属性
     * @param subAggField 二维属性
     * @param topN 返回排名topN限制，小于等于0默认最大返回10
     * @param filterParams 过滤参数
     * @param timeRange 时间范围
     * @param param 全文检索key，不需要则不填
     * @param paramKeys 全文见说被检索的属性集
     * @return
     */
    public List<Map<String, Object>> aggregateCount(String[] indices, String aggField, String subAggField,
                                                    int topN, Map<String, Object[]> filterParams,
                                                    Map<String, String[]> timeRange, String param,
                                                    String[] paramKeys) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.size(0);
        if (StringUtils.isNotEmpty(aggField)) {
            TermsAggregationBuilder aggregationBuilder = AggregationBuilders
                .terms(aggField)
                .field(aggField);

            if (topN > 0) {
                aggregationBuilder.size(topN);
            } else {
                aggregationBuilder.size(TOP_MAX_VALUE);
            }

            // 生成子聚合语句
            if (StringUtils.isNotEmpty(subAggField)) {
                TermsAggregationBuilder subBuilder = AggregationBuilders
                    .terms(subAggField)
                    .field(subAggField)
                    .size(TOP_MAX_VALUE);
                aggregationBuilder.subAggregation(subBuilder);
            }
            sourceBuilder.aggregation(aggregationBuilder);
        }

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        setFilterParams(boolQueryBuilder, filterParams);
        setTimeRange(boolQueryBuilder, timeRange);
        if (param != null && paramKeys != null) {
            boolQueryBuilder.filter(QueryBuilders.multiMatchQuery(param, paramKeys));
        }
        sourceBuilder.query(boolQueryBuilder);

        SearchRequest request = new SearchRequest(indices);
        request.source(sourceBuilder);

        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            return processAggregateResult(AGGREGATE_TYPE, response, aggField, subAggField);
        } catch (IOException e) {
            logger.error("MultiAggregation Failed! ", e.getMessage());
        }
        return null;
    }

    public List<Map<String, Object>> aggregateFuzzyCount(String[] indices, String aggField, String subAggField,
                                                         int topN, BoolQueryBuilder boolBuilder,
                                                         Map<String, String[]> timeRange) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);

        if (StringUtils.isNotEmpty(aggField)) {
            TermsAggregationBuilder aggregationBuilder = AggregationBuilders
                .terms(aggField)
                .field(aggField).size(topN > 0 ? topN : TOP_MAX_VALUE);

            if (StringUtils.isNotEmpty(subAggField)) {
                TermsAggregationBuilder subBuilder = AggregationBuilders
                    .terms(subAggField)
                    .field(subAggField)
                    .size(TOP_MAX_VALUE);
                aggregationBuilder.subAggregation(subBuilder);
            }
            sourceBuilder.aggregation(aggregationBuilder);
        }

        setTimeRange(boolBuilder, timeRange);
        sourceBuilder.query(boolBuilder);

        SearchRequest request = new SearchRequest(indices);
        request.source(sourceBuilder);

        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            return processAggregateResult(AGGREGATE_TYPE, response, aggField, subAggField);
        } catch (IOException e) {
            logger.error("ES AggregateFuzzy Search Failed! ", e.getMessage());
        }
        return null;
    }

    /**
     * @param indices
     * @param aggField
     * @param subAggField
     * @param topN
     * @param filterParams
     * @param timeRange
     * @return
     */
    public List<Map<String, Object>> aggregateStats(String[] indices, String aggField, String subAggField,
                                                   int topN, Map<String, Object[]> filterParams,
                                                   Map<String, String[]> timeRange) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);

        if (StringUtils.isNotEmpty(aggField)) {
            TermsAggregationBuilder aggregationBuilder = AggregationBuilders
                .terms(aggField)
                .field(aggField)
                .size(topN > 0 ? topN : TOP_MAX_VALUE);

            if (StringUtils.isNotEmpty(subAggField)) {
                StatsAggregationBuilder statsAggregationBuilder = AggregationBuilders
                    .stats(subAggField)
                    .field(subAggField);
                aggregationBuilder.subAggregation(statsAggregationBuilder);
            }

            sourceBuilder.aggregation(aggregationBuilder);
        }

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        setTimeRange(queryBuilder, timeRange);
        setFilterParams(queryBuilder, filterParams);
        sourceBuilder.query(queryBuilder);

        SearchRequest request = new SearchRequest(indices);
        request.source(sourceBuilder);

        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            return processAggregateStatsResult(response, aggField, subAggField);
        } catch (IOException e) {
            logger.error("ES aggregateStats Failed! ", e.getMessage());
        }
        return null;
    }

    /**
     * 根据时间进行分组统计，支持二级属性分组
     * @param indices
     * @param timeField
     * @param timeFormat
     * @param subField
     * @param interval
     * @param filterParams
     * @param timeRange
     * @return
     */
    public List aggregateDateHistogram(String[] indices, String timeField, String timeFormat, String subField,
                                       long interval, Map<String, Object[]> filterParams, Map<String, String[]> timeRange) {
        return aggregateDateHistogram(indices, timeField, timeFormat, subField, interval, filterParams, timeRange,
            null, null, null, null);
    }

    /**
     * 自然日、月、年等的统计
     * @param indices
     * @param timeField
     * @param timeFormat
     * @param subField
     * @param interval
     * @param filterParams
     * @param timeRange
     * @param dateHistogramInterval
     * @param order
     * @return
     */
    public List aggregateDateHistogram(String[] indices, String timeField, String timeFormat, String subField,
                                       long interval, Map<String, Object[]> filterParams, Map<String, String[]> timeRange,
                                       DateHistogramInterval dateHistogramInterval, BucketOrder order) {
        return aggregateDateHistogram(indices, timeField, timeFormat, subField, interval, filterParams, timeRange,
            dateHistogramInterval, order, null, null);
    }

    /**
     * 带全文检索的过滤
     * @param indices
     * @param timeField
     * @param timeFormat
     * @param subField
     * @param interval
     * @param filterParams
     * @param timeRange
     * @param param
     * @param paramKeys
     * @return
     */
    public List aggregateDateHistogram(String[] indices, String timeField, String timeFormat, String subField,
                                       long interval, Map<String, Object[]> filterParams, Map<String, String[]> timeRange,
                                       String param, String[] paramKeys) {
        return aggregateDateHistogram(indices, timeField, timeFormat, subField, interval, filterParams, timeRange,
            null, null, param, paramKeys);
    }

    /**
     * 对时间进行聚合查询，支持排序和全文检索
     * @param indices
     * @param timeField
     * @param timeFormat
     * @param subField
     * @param interval
     * @param filterParams
     * @param timeRange
     * @param dateHistogramInterval
     * @param order
     * @param param
     * @param paramKeys
     * @return
     */
    public List aggregateDateHistogram(String[] indices, String timeField, String timeFormat, String subField,
                                       long interval, Map<String, Object[]> filterParams, Map<String, String[]> timeRange,
                                       DateHistogramInterval dateHistogramInterval, BucketOrder order, String param,
                                       String[] paramKeys) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);
        if (StringUtils.isNotEmpty(timeField)) {
            // 设置日期聚合时间范围
            ExtendedBounds extendedBounds = getExtendedBounds(timeField, timeField, timeRange);

            // 生成日期聚合语句
            DateHistogramAggregationBuilder aggregationBuilder = AggregationBuilders
                .dateHistogram(timeField)
                .field(timeField)
                .interval(interval)
                .format(timeFormat)
                .minDocCount(0)
                .extendedBounds(extendedBounds)
                .timeZone(DateTimeZone.forOffsetHours(8));
            if (dateHistogramInterval != null) {
                aggregationBuilder = aggregationBuilder.dateHistogramInterval(dateHistogramInterval);
                if (order != null) {
                    aggregationBuilder = aggregationBuilder.order(order);
                }
            }

            if (StringUtils.isNotEmpty(subField)) {
                TermsAggregationBuilder subBuider = AggregationBuilders
                    .terms(subField)
                    .field(subField)
                    .size(TOP_MAX_VALUE);
                aggregationBuilder.subAggregation(subBuider);
            }
            sourceBuilder.aggregation(aggregationBuilder);
        }

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        setFilterParams(queryBuilder, filterParams);

        if (param!= null && paramKeys != null) {
            queryBuilder.filter(QueryBuilders.multiMatchQuery(param, paramKeys));
        }
        setTimeRange(queryBuilder, timeRange);
        sourceBuilder.query(queryBuilder);

        SearchRequest request = new SearchRequest(indices);
        request.source(sourceBuilder);

        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            return processAggregateResult(AGGREGATE_TYPE, response, timeField, subField);
        } catch (IOException e) {
            logger.error("ES Failed! ", e.getMessage());
            return null;
        }
    }

    /**
     * 根据ID查询文档数据
     * @param indices 索引
     * @param ids id
     * @param sortParams 排序
     * @param size 查询结果数量
     * @return 文档数据内容
     */
    public List<Map<String, Object>> queryDocByIds(String[] indices, String[] ids, Map<String, SortOrder> sortParams,
                                                   int size) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(size);

        // 设置ID查询条件
        IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery().addIds(ids);
        sourceBuilder.query(idsQueryBuilder);

        // 设置排序条件
        if (sortParams != null) {
            sortParams.forEach((field, order) -> sourceBuilder.sort(field, order));
        }

        SearchRequest request = new SearchRequest(indices);
        request.source(sourceBuilder);
        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            return processQueryResult(response);
        } catch (IOException e) {
            logger.error("ES searchIDs Failed! ", e.getMessage());
            return null;
        }
    }

    /**
     * 查询文档，根据过滤条件进行查询
     * @param indices 索引
     * @param filterParams 过滤条件
     * @param timeRange 时间范围
     * @param sortParams 排序条件
     * @param size 返回内容大小
     * @return
     */
    public List<Map<String, Object>> queryDocs(String[] indices, Map<String, Object[]> filterParams,
                                               Map<String, String[]> timeRange, Map<String, SortOrder> sortParams,
                                               int size) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(size);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        setTimeRange(boolQueryBuilder, timeRange);
        setFilterParams(boolQueryBuilder, filterParams);
        sourceBuilder.query(boolQueryBuilder);

        if (sortParams != null) {
            sortParams.forEach((field, order) -> sourceBuilder.sort(field, order));
        }

        SearchRequest request = new SearchRequest(indices);
        request.source(sourceBuilder);

        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            return processQueryResult(response);
        } catch (IOException e) {
            logger.error("ES QueryDocs Failed! ", e.getMessage());
            return null;
        }
    }

    /**
     * 分页查询
     * @param indices
     * @param filterParams
     * @param timeRange
     * @param sortParams
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageInfo<List<Map<String, Object>>> findAllPageInfo(String[] indices,
                                                               Map<String, Object[]> filterParams,
                                                               Map<String, String[]> timeRange,
                                                               Map<String, SortOrder> sortParams,
                                                               int pageNo, int pageSize) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(getFrom(pageNo, pageSize));
        sourceBuilder.size(pageSize);

        BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery();
        setFilterParams(boolQueryBuilder, filterParams);
        setTimeRange(boolQueryBuilder, timeRange);
        sourceBuilder.query(boolQueryBuilder);

        if (sortParams != null) {
            sortParams.forEach((name, order) -> sourceBuilder.sort(name, order));
        }

        SearchRequest request = new SearchRequest(indices);
        request.source(sourceBuilder);

        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            return buildPageInfo(response, pageNo, pageSize);
        } catch (IOException e) {
            logger.error("ES findAllPageInfo failed! ", e.getMessage());
            return null;
        }
    }

    /**
     * 生成时间范围过滤器
     * @param boolBuilder
     * @param timeRange key-value中：value的值要大于一个，但是如果大于2，只取前两个值
     */
    private void setTimeRange(BoolQueryBuilder boolBuilder, Map<String, String[]> timeRange) {
        if (timeRange != null && timeRange.size() > 0) {
            timeRange.forEach(
                (dateField, range) -> {
                    if (range != null && range.length == 2) {
                        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(dateField);
                        if (range[0] != null) {
                            rangeQueryBuilder.from(formatTime(range[0]));
                        }
                        if (range[1] != null) {
                            rangeQueryBuilder.to(formatTime(range[1]));
                        }
                        boolBuilder.filter(rangeQueryBuilder);
                    }
                }
            );
        }
    }

    /**
     * 生成时间范围过滤器，只对单个范围过滤器有效
     * @param boolBuilder
     * @param dateField 日期域
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    private void setTimeRange(BoolQueryBuilder boolBuilder, String dateField, String startTime, String endTime) {
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(dateField);
        rangeQueryBuilder.from();
        rangeQueryBuilder.to();
        boolBuilder.filter(rangeQueryBuilder);
    }

    /**
     * 生成属性过滤器
     * @param boolBuilder bool过滤
     * @param queryParams 属性映射
     */
    private void setFilterParams(BoolQueryBuilder boolBuilder, Map<String, Object[]> queryParams) {
        if (queryParams != null && queryParams.size() > 0) {
            queryParams.forEach(
                (field, params) -> {
                    TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(field, params);
                    boolBuilder.filter(termQueryBuilder);
                }
            );
        }
    }

    /**
     * 将输入的特定时间格式转换成字符格式
     * @param timeString
     * @return
     */
    private String formatTime(String timeString) {
        // quick表达式或带时区的时间表达式
        if (timeString.indexOf("now") != -1 || timeString.indexOf("T") != -1) {
            return timeString;
        }

        try {
            Date date = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse(timeString);
            return FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSSZ").format(date);
        } catch (ParseException e) {
            logger.error("formatTime Failed! ", e.getMessage());
        }
        return null;
    }

    /**
     * 处理聚合结果，可以处理子聚合结果
     * @param type 聚合类型
     * @param response 聚合原始结果
     * @param aggField 一级聚合属性
     * @param subAggField 二级聚合属性
     * @return
     */
    private List<Map<String, Object>> processAggregateResult(String type, SearchResponse response,
                                                             String aggField, String subAggField) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (response != null) {
            Aggregations aggregations = response.getAggregations();
            // 处理聚合结果
            if (aggregations != null) {
                // 获得聚合结果集，对结果进行处理
                MultiBucketsAggregation termAgg = aggregations.get(aggField);
                Iterator termIt = termAgg.getBuckets().iterator();

                while (termIt.hasNext()) {
                    Map<String, Object> value = new HashMap<>();
                    Object bucket = termIt.next();
                    String key;
                    long count = 0L;

                    if (AGGREGATE_TYPE.equals(type)) {
                        key = ((MultiBucketsAggregation.Bucket)bucket).getKeyAsString();
                        count = ((MultiBucketsAggregation.Bucket)bucket).getDocCount();
                    } else {
                        key = ((ParsedAdjacencyMatrix.ParsedBucket)bucket).getKeyAsString();
                        count = ((ParsedAdjacencyMatrix.ParsedBucket)bucket).getDocCount();
                    }

                    if (StringUtils.isNotEmpty(subAggField)) {
                        // 得到所有子聚合
                        MultiBucketsAggregation subAgg = ((HasAggregations)bucket).getAggregations().get(subAggField);
                        Iterator subAggIt = subAgg.getBuckets().iterator();
                        List subValueList = new ArrayList();
                        while (subAggIt.hasNext()) {
                            Map subValue = new HashMap();
                            MultiBucketsAggregation.Bucket subBucket = (MultiBucketsAggregation.Bucket)subAggIt.next();
                            String subKey = subBucket.getKeyAsString();
                            long subCount = subBucket.getDocCount();
                            subValue.put("name", subKey);
                            subValue.put("value", subCount);
                            subValueList.add(subValue);
                        }
                        value.put("detail", subValueList);
                    }
                    value.put("name", key);
                    value.put("value", count);
                    result.add(value);
                }
            }
        }
        return result;
    }

    /**
     *
     * @param response
     * @param aggField
     * @param subAggField
     * @return
     */
    private List<Map<String, Object>> processAggregateStatsResult(SearchResponse response, String aggField, String subAggField) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (response != null) {
            Aggregations aggregations = response.getAggregations();
            if (aggregations != null) {
                MultiBucketsAggregation teamAgg = aggregations.get(aggField);
                Iterator teamBucketIt = teamAgg.getBuckets().iterator();

                while (teamBucketIt.hasNext()) {
                    Map<String, Object> value = new HashMap<>();

                    Object bucket = teamBucketIt.next();
                    String key = ((MultiBucketsAggregation.Bucket)bucket).getKeyAsString();
                    long count = ((MultiBucketsAggregation.Bucket)bucket).getDocCount();

                    if (StringUtils.isNotEmpty(subAggField)) {
                        Aggregations subAggregates = ((ParsedSignificantStringTerms.ParsedBucket)bucket).getAggregations();
                        ParsedStats parsedStats = subAggregates.get(subAggField);
                        value.put("parsedStats", parsedStats);
                    }
                    value.put("name", key);
                    value.put("value", count);
                    result.add(value);
                }
            }
        }
        return result;
    }

    /**
     * 设置日期聚合的时间范围
     * @param timeField 日期属性
     * @param timeFormat 日期格式
     * @param timeRange 日期范围
     * @return
     */
    private ExtendedBounds getExtendedBounds(String timeField, String timeFormat, Map<String, String[]> timeRange) {
        String min = null;
        String max = null;

        if (timeRange != null && timeRange.containsKey(timeField)) {
            String[] range = timeRange.get(timeField);
            if (range != null && range.length == 2) {
                if (range[0] != null) {
                    min = formatTime(timeFormat, range[0]);
                }
                if (range[1] != null) {
                    max = formatTime(timeFormat, range[1]);
                }
            }
        }
        return new ExtendedBounds(min, max);
    }

    /**
     * 时间格式转换
     * @param timeFormat
     * @param timeString
     * @return
     */
    private String formatTime(String timeFormat, String timeString) {
        String format = null;

        if (timeString.indexOf("now") != -1) {
            return timeString;
        } else if (timeString.indexOf("T") != -1) {
            format = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        } else {
            format = "yyyy-MM-dd HH:mm:ss";
        }

        try {
            Date date = FastDateFormat.getInstance(format).parse(timeString);
            return FastDateFormat.getInstance(timeFormat).format(date);
        } catch (ParseException e) {
            logger.error("FormatTime Failed! ", e.getMessage());
        }
        return null;
    }

    private List<Map<String, Object>> processQueryResult(SearchResponse response) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (response != null) {
            Iterator<SearchHit> iterator = response.getHits().iterator();
            while (iterator.hasNext()) {
                SearchHit searchHit = iterator.next();
                result.add(searchHit.getSourceAsMap());
            }
        }

        return result;
    }

    /**
     * 设置分页查询的起始页码
     * @param pageNo
     * @param pageSize
     * @return
     */
    private int getFrom(int pageNo, int pageSize) {
        int from = (pageNo - 1) * pageSize;
        if (from >= 0) {
            return from;
        }
        return 0;
    }

    /**
     * 对返回结果进行分页构建
     * @param response
     * @param pageNo
     * @param pageSize
     * @return
     */
    private PageInfo<List<Map<String, Object>>> buildPageInfo(SearchResponse response, int pageNo, int pageSize) {
        PageInfo<List<Map<String, Object>>> pageInfo = new PageInfo<>();

        int total = ((Long)response.getHits().getTotalHits()).intValue();
        int pages = buildPages(pageSize, total);

        pageInfo.setPageNum(pageNo);
        pageInfo.setPageSize(pageSize);
        pageInfo.setSize(buildCurrentPageSize(pageNo, pageSize, total));
        pageInfo.setStartRow((pageNo-1) * pageSize + 1);
        pageInfo.setEndRow((pageNo-1) * pageSize + buildCurrentPageSize(pageNo, pageSize, total));
        pageInfo.setTotal(total);
        pageInfo.setPages(pages);
        pageInfo.setList((List)processQueryResult2(response));
        pageInfo.setPrePage(pageNo - 1);
        pageInfo.setNextPage(pageNo + 1);
        pageInfo.setIsFirstPage(pageNo == 1);
        pageInfo.setIsLastPage(pageNo == pages);
        pageInfo.setHasPreviousPage(pageNo > 1);
        pageInfo.setHasNextPage(pageNo != pages);
        pageInfo.setNavigatePages(8);
        pageInfo.setNavigatepageNums(buildNavigatePageNums(pages));
        pageInfo.setNavigateFirstPage(pages > 0 ? 1 : 0);
        pageInfo.setNavigateLastPage(pages);
        return pageInfo;
    }

    /**
     * 生成总页数
     * @param pageSize
     * @param total
     * @return
     */
    private int buildPages(int pageSize, int total) {
        if (total > 0) {
            return total / pageSize - 1;
        }
        return 0;
    }

    /**
     * 构建当前页码
     * @param pageNum
     * @param pageSize
     * @param total
     * @return
     */
    private int buildCurrentPageSize(int pageNum, int pageSize, int total) {
        if (total > pageNum * pageSize) {
            return pageSize;
        } else {
            return total % pageSize;
        }
    }

    /**
     * 构建所有导航页码
     * @param pages
     * @return
     */
    private int[] buildNavigatePageNums(int pages) {
        int[] navigatePageNums = new int[pages];

        for (int i = 0; i < pages; i++) {
            navigatePageNums[i] = i + 1;
        }
        return navigatePageNums;
    }

    /**
     * 高亮显示查询结果
     * @param response
     * @return
     */
    private List<Map<String, Object>> processQueryResult2(SearchResponse response) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (response != null) {
            Iterator<SearchHit> iterator = response.getHits().iterator();
            while (iterator.hasNext()) {
                SearchHit searchHit = iterator.next();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();

                for (String key: sourceAsMap.keySet()) {
                    HighlightField nameField = highlightFields.get(key);
                    if (nameField != null) {
                        Text[] fragments = nameField.fragments();
                        String nameTmp = "";
                        for (Text text: fragments) {
                            nameTmp += text;
                        }
                        sourceAsMap.put(key, nameTmp);
                    }
                }
                sourceAsMap.put("id", searchHit.getId());
                result.add(sourceAsMap);
            }
        }
        return result;
    }
}