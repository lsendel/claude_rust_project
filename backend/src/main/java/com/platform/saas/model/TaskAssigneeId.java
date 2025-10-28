package com.platform.saas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key for TaskAssignee entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssigneeId implements Serializable {

    private UUID taskId;
    private UUID userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskAssigneeId that = (TaskAssigneeId) o;
        return Objects.equals(taskId, that.taskId) &&
               Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, userId);
    }
}
