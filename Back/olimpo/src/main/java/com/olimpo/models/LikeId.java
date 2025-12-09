package com.olimpo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeId implements Serializable {

    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "idea_id")
    private Integer ideaId;
}
