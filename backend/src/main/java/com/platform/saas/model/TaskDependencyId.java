package com.platform.saas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key for TaskDependency entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDependencyId implements Serializable {

    private UUID blockingTaskId;
    private UUID blockedTaskId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskDependencyId that = (TaskDependencyId) o;
        return Objects.equals(blockingTaskId, that.blockingTaskId) &&
               Objects.equals(blockedTaskId, that.blockedTaskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockingTaskId, blockedTaskId);
    }
}
