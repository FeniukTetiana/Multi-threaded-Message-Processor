package ua.shpp.feniuk.pojo;

import jakarta.validation.constraints.*;
import ua.shpp.feniuk.validation.ValidEDDR;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class PojoMessage implements Serializable {
    @NotNull(message = "Name must not be null")
    @Size(min = 7, message = "Name must be ≥ 7 chars")
    @Pattern(regexp = ".*[aA]+.*", message = "Name needs 'aA'")
    private String name;

    @NotNull(message = "EDDR must not be null")
    @ValidEDDR
    private String eddr;

    @Min(value = 10, message = "Count must be ≥ 10")
    private int count;

    @NotNull(message = "CreatedAt must not be null")
    private LocalDateTime createdAt;

    private String errors;

    public PojoMessage() {}

    public PojoMessage(String name, String eddr, int count, LocalDateTime createdAt) {
        this.name = name;
        this.eddr = eddr;
        this.count = count;
        this.createdAt = createdAt;
    }

    public PojoMessage(String name, int count, String errors) {
        this.name = name;
        this.count = count;
        this.errors = errors;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEddr() {
        return eddr;
    }

    public void setEddr(String eddr) {
        this.eddr = eddr;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "PojoMessage{" +
                "name='" + name + '\'' +
                ", eddr='" + eddr + '\'' +
                ", count=" + count +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PojoMessage that = (PojoMessage) o;
        return count == that.count && Objects.equals(name, that.name) && Objects.equals(eddr, that.eddr)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, eddr, count, createdAt);
    }
}


