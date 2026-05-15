package com.itheima.ms.search.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.search.domain.CourierTaskDTO;
import com.itheima.chaincloud.domain.search.domain.CourierTaskPageQueryDTO;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskIsDeletedEnum;
import com.itheima.ms.search.entity.CourierTaskEntity;
import com.itheima.ms.search.service.CourierTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.itheima.ms.search.enums.ExceptionEnum.ES_ACCESS_ERROR;
import static com.itheima.ms.search.enums.ExceptionEnum.TASK_NOT_FOUND;

/**
 * 快递员任务服务实现类
 */
@Slf4j
@Service
public class CourierTaskServiceImpl implements CourierTaskService {
    @Resource
    private ElasticsearchClient client;
    @Value("${cc.es.index_name}")
    private String indexName;

    //定义该类中所用的常量
    private static final String TASK_TYPE = "taskType";
    private static final String STATUS = "status";
    private static final String COURIER_ID = "courierId";
    private static final String TRANSPORT_ORDER_ID = "transportOrderId";
    private static final String ORDER_ID = "orderId";
    private static final String PHONE = "phone";
    private static final String NAME = "name";
    private static final String CREATED = "created";
    private static final String IS_DELETED = "isDeleted";

    @Override
    public PageResponse<CourierTaskDTO> pageQuery(CourierTaskPageQueryDTO pageQueryDTO) {
        // 1.取出查询的关键词
        String keyWord = pageQueryDTO.getKeyword();

        // 2.开始构造查询条件
        // 2.1 对运单号、手机号、姓名模糊查询
        MultiMatchQuery.Builder multiMatchQuery = new MultiMatchQuery.Builder();
        multiMatchQuery.fields(TRANSPORT_ORDER_ID, PHONE, NAME).query(keyWord);

        Query.Builder query1 = new Query.Builder();
        query1.multiMatch(multiMatchQuery.build());

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        boolQuery.must(query1.build());

        // 2.2过滤快递员id
        Query.Builder query2 = new Query.Builder();
        query2.term(getTermQuery(COURIER_ID, pageQueryDTO.getCourierId().toString()));
        boolQuery.filter(query2.build());

        // 2.3 过滤不是逻辑删除的
        Query.Builder query3 = new Query.Builder();
        query3.term(getTermQuery(IS_DELETED, PickupDispatchTaskIsDeletedEnum.NOT_DELETED.getCode().toString()));
        boolQuery.filter(query3.build());

        // 2.4 对 taskType 字段判空，如果存在则过滤任务类型
        if (ObjectUtil.isNotEmpty(pageQueryDTO.getTaskType())) {
            Query.Builder query4 = new Query.Builder();
            query4.term(getTermQuery(TASK_TYPE, pageQueryDTO.getTaskType().toString()));
            boolQuery.filter(query4.build());
        }

        // 2.5 对 status 字段判空，如果存在则过滤任务类型
        if (ObjectUtil.isNotEmpty(pageQueryDTO.getStatus())) {
            Query.Builder query4 = new Query.Builder();
            query4.term(getTermQuery(STATUS, pageQueryDTO.getStatus().toString()));
            boolQuery.filter(query4.build());
        }

        // 3.使用client查询，根据 created 字段倒序排列，并进行分页
        SearchResponse<CourierTaskEntity> response;
        try {
            response = client.search(s -> s
                            .index(indexName)
                            .query(q -> q.bool(boolQuery.build()))
                            .sort(sortOptionsBuilder -> sortOptionsBuilder
                                    .field(fieldSortBuilder -> fieldSortBuilder
                                            .field(CREATED)
                                            .order(SortOrder.Desc)))
                            .from((pageQueryDTO.getPage() - 1) * pageQueryDTO.getPgeSize())
                            .size(pageQueryDTO.getPgeSize())
                    , CourierTaskEntity.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CCException(ES_ACCESS_ERROR);
        }

        // 4.解析响应
        List<CourierTaskEntity> entities = handleResponse(response);
        if (CollUtil.isEmpty(entities)) {
            return new PageResponse<>();
        }

        // 5.构建响应结果
        // 5.1.获取总条数
        long total = response.hits().total().value();
        long pages = (long) Math.ceil(total * 1.0 / pageQueryDTO.getPgeSize());

        // 5.2实体类转换为dto
        List<CourierTaskDTO> list = BeanUtil.copyToList(entities, CourierTaskDTO.class);

        // 5.3组装分页结果
        return PageResponse.of(list, pageQueryDTO.getPage(), pageQueryDTO.getPgeSize(), pages, total);
    }

    /**
     * 构造TermQuery
     *
     * @param fieldName 字段名
     * @param keyWord   关键词
     * @return TermQuery对象
     */
    private TermQuery getTermQuery(String fieldName, String keyWord) {
        return new TermQuery.Builder().field(fieldName).value(keyWord).build();
    }

    /**
     * 新增快递员任务
     *
     * @param courierTaskDTO 快递员任务
     */
    @Override
    public void saveOrUpdate(CourierTaskDTO courierTaskDTO) {
        // 1.构造对象
        CourierTaskEntity courierTaskEntity = BeanUtil.toBean(courierTaskDTO, CourierTaskEntity.class);

        // 2.使用DSL语法创建对象，进行新增或更新
        IndexRequest<CourierTaskEntity> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(String.valueOf(courierTaskDTO.getId()))
                .document(courierTaskEntity));

        // 3.发送请求，全量更新
        try {
            client.index(request);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CCException(ES_ACCESS_ERROR);
        }
    }

    /**
     * 根据取派件id查询快递员任务
     *
     * @param id 取派件id
     * @return 快递员任务
     */
    @Override
    public CourierTaskDTO findById(Long id) {
        // 1.根据id查询快递员任务
        GetResponse<CourierTaskEntity> response;
        try {
            response = client.get(g -> g.index(indexName).id(String.valueOf(id)), CourierTaskEntity.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CCException(ES_ACCESS_ERROR);
        }

        // 2.判断响应数据是否为空
        if (response.found()) {
            return BeanUtil.toBean(response.source(), CourierTaskDTO.class);
        }

        throw new CCException(TASK_NOT_FOUND);
    }

    /**
     * 根据订单id查询快递员任务
     *
     * @param orderId 订单id
     * @return 快递员任务列表
     */
    @Override
    public List<CourierTaskEntity> findByOrderId(Long orderId) {
        // 1.构造查询条件
        Query query = new Query.Builder().term(t -> t
                .field(ORDER_ID)
                .value(String.valueOf(orderId))).build();

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(indexName)
                .query(query).build();

        // 2.使用client查询
        SearchResponse<CourierTaskEntity> response;
        try {
            response = client.search(searchRequest, CourierTaskEntity.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CCException(ES_ACCESS_ERROR);
        }

        // 3.提取查询结果条数
        long total = response.hits().total().value();

        // 3.1如果查询结果条数为0，抛出异常
        if (ObjectUtil.equal(total, 0L)) {
            throw new CCException(TASK_NOT_FOUND);
        }

        // 3.2如果查询结果条数不为0，提取结果
        return handleResponse(response);
    }

    /**
     * 解析响应结果
     *
     * @param response 搜索响应结果
     * @return 快递员任务列表
     */
    private List<CourierTaskEntity> handleResponse(SearchResponse<CourierTaskEntity> response) {
        List<CourierTaskEntity> entities = new ArrayList<>();
        List<Hit<CourierTaskEntity>> hits = response.hits().hits();
        for (Hit<CourierTaskEntity> hit : hits) {
            CourierTaskEntity courierTaskEntity = hit.source();
            entities.add(courierTaskEntity);
        }
        return entities;
    }
}
