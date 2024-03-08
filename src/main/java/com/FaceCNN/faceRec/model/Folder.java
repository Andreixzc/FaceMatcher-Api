package com.FaceCNN.faceRec.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String folderPath;

    @Column(unique = true)
    private String folderPklPath;

    @Column
    private String folderName;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolderContent> folderContents = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
    }

    public UUID userId() {
        return user.getId();
    }

    public void addFolderContent(FolderContent folderContent) {
        folderContents.add(folderContent);
        folderContent.setFolder(this);
    }

    public void removeFolderContent(FolderContent folderContent) {
        folderContents.remove(folderContent);
        folderContent.setFolder(null);
    }
}
