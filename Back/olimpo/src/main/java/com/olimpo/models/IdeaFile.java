package com.olimpo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "IDEA_FILES")
@Data
@NoArgsConstructor
public class IdeaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idea_id", nullable = false)
    @JsonIgnore
    private Idea idea;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_url", length = 512, nullable = false)
    private String fileUrl;

    public IdeaFile(Idea idea, String fileName, String fileType, String fileUrl) {
        this.idea = idea;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileUrl = fileUrl;
    }
}