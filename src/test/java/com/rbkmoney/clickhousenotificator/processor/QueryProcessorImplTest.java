package com.rbkmoney.clickhousenotificator.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.clickhousenotificator.TestObjectsFactory;
import com.rbkmoney.clickhousenotificator.dao.AbstractPostgresIntegrationTest;
import com.rbkmoney.clickhousenotificator.dao.domain.enums.NotificationStatus;
import com.rbkmoney.clickhousenotificator.dao.domain.enums.ReportStatus;
import com.rbkmoney.clickhousenotificator.dao.domain.tables.pojos.Notification;
import com.rbkmoney.clickhousenotificator.dao.domain.tables.pojos.Report;
import com.rbkmoney.clickhousenotificator.dao.pg.*;
import com.rbkmoney.clickhousenotificator.domain.QueryResult;
import com.rbkmoney.clickhousenotificator.query.TestQuery;
import com.rbkmoney.clickhousenotificator.resource.NotificationResourceImpl;
import com.rbkmoney.clickhousenotificator.serializer.QueryResultSerde;
import com.rbkmoney.clickhousenotificator.service.MailSenderServiceImpl;
import com.rbkmoney.clickhousenotificator.service.NotificationServiceImpl;
import com.rbkmoney.clickhousenotificator.service.QueryService;
import com.rbkmoney.clickhousenotificator.service.factory.CsvAttachmentFactory;
import com.rbkmoney.clickhousenotificator.service.factory.MailFactory;
import com.rbkmoney.clickhousenotificator.service.filter.ChangeQueryResultFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {NotificationDaoImpl.class, NotificationResourceImpl.class, ChannelDaoImpl.class,
        ReportNotificationDaoImpl.class,
        QueryProcessorImpl.class, NotificationDaoImpl.class, QueryResultSerde.class, ObjectMapper.class,
        NotificationServiceImpl.class, MailFactory.class, CsvAttachmentFactory.class, ChangeQueryResultFilter.class})
@Disabled("Надо доделать")
class QueryProcessorImplTest extends AbstractPostgresIntegrationTest {

    @Autowired
    NotificationDao notificationDao;
    @Autowired
    NotificationResourceImpl notificationResource;
    @Autowired
    ChannelDaoImpl channelDao;
    @Autowired
    ReportNotificationDao reportNotificationDao;
    @Autowired
    QueryProcessorImpl queryProcessor;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    MailSenderServiceImpl mailSenderServiceImpl;

    @MockBean
    QueryService queryService;


    @BeforeEach
    public void init() {
        channelDao.insert(TestObjectsFactory.testChannel());
        Notification successNotify =
                TestObjectsFactory.testNotification("successNotify", TestQuery.QUERY_METRIC_RECURRENT,
                        NotificationStatus.ACTIVE, TestObjectsFactory.CHANNEL, "shopId,currency");
        notificationResource.createOrUpdate(successNotify);
        notificationResource
                .createOrUpdate(
                        TestObjectsFactory.testNotification("failedName", "select * from analytic.events_sink_refund",
                                NotificationStatus.ACTIVE, "errorChannel", "test"));
        when(queryService.query(anyString()))
                .thenReturn(List.of(Map.of("shopId", "ad8b7bfd-0760-4781-a400-51903ee8e504")));

    }

    @Test
    void process() throws Exception {
        queryProcessor.process();

        List<Report> notificationByStatus = reportNotificationDao.getNotificationByStatus(ReportStatus.send);

        String result = notificationByStatus.get(0).getResult();
        QueryResult queryResult = objectMapper.readValue(result, QueryResult.class);
        assertEquals("ad8b7bfd-0760-4781-a400-51903ee8e504", queryResult.getResults().get(0).get("shopId"));


        queryProcessor.process();

        notificationByStatus = reportNotificationDao.getNotificationByStatus(ReportStatus.created);
        assertEquals(0L, notificationByStatus.size());

        Thread.sleep(1000L);

        queryProcessor.process();
        notificationByStatus = reportNotificationDao.getNotificationByStatus(ReportStatus.skipped);
        assertEquals(0L, notificationByStatus.size());

        verify(mailSenderServiceImpl, times(1)).send(any());

    }

}