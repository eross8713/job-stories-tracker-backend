package com.ericross.backend.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ericross.backend.model.Story;

public interface StoryRepository extends JpaRepository<Story, UUID> {
}
