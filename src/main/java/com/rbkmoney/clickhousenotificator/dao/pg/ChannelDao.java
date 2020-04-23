package com.rbkmoney.clickhousenotificator.dao.pg;


import com.rbkmoney.clickhousenotificator.dao.domain.tables.pojos.Channel;

public interface ChannelDao {

    String insert(Channel listRecord);

    void remove(String id);

    void remove(Channel listRecord);

    Channel getByName(String name);

}