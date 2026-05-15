package com.itheima.ms.transportinfo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("cc_transport_info")
public class TransportInfoEntity {

    @Id
    @JsonIgnore
    private ObjectId id;
    @Indexed
    private String transportOrderId;
    private List<TransportInfoDetail> infoList;
    private Long created;
    private Long updated;
}
