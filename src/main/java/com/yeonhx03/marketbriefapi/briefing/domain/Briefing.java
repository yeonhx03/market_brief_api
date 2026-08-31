package com.yeonhx03.marketbriefapi.briefing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "briefings")
public class Briefing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schema_version", nullable = false)
    private Integer schemaVersion;

    @Column(name = "briefing_type", nullable = false, length = 64)
    private String briefingType;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Briefing() {
    }

    public Briefing(Integer schemaVersion, String briefingType, String payload) {
        this.schemaVersion = schemaVersion;
        this.briefingType = briefingType;
        this.payload = payload;
    }

    public Long getId() {
        return id;
    }

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public String getBriefingType() {
        return briefingType;
    }

    public String getPayload() {
        return payload;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
