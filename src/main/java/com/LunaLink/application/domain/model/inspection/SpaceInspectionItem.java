package com.LunaLink.application.domain.model.inspection;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "space_inspection_item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpaceInspectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @JsonProperty("equipmentName")
    @Column(nullable = false)
    private String equipmentName;

    @JsonProperty("okConfirmed")
    @Column(nullable = false)
    private boolean okConfirmed;

    @JsonProperty("voucherKey")
    @Column(nullable = false)
    private String voucherKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspection_id", nullable = false)
    private SpaceInspection inspection;

    public SpaceInspectionItem(String equipmentName, boolean okConfirmed, String voucherKey) {
        this.equipmentName = equipmentName;
        this.okConfirmed = okConfirmed;
        this.voucherKey = voucherKey;
    }

    public void setInspection(SpaceInspection inspection) {
        this.inspection = inspection;
    }
}
