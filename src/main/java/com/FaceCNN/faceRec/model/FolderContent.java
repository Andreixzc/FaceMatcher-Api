package com.FaceCNN.faceRec.model;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FolderContent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String filePath;

    @Column(unique = true)
    private String pklFilePath;

    @Column(nullable = true)
    private String URL;

    @Column(nullable = true)
    private String fileName;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    
    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;

    public UUID folderId() {
        return folder.getId();
    }


    @PrePersist
    public void prePersist() {
        this.createdOn = new Date();
    }
}