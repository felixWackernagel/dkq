package de.wackernagel.dkq.room;

import androidx.room.PrimaryKey;

import java.util.Objects;

public abstract class BaseEntity {

    @PrimaryKey( autoGenerate = true)
    private long id;

    public BaseEntity( long id ) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    protected void setId(long id ) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "id=" + id +
                '}';
    }
}
