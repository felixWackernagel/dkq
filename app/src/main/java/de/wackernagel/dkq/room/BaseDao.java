package de.wackernagel.dkq.room;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import static androidx.room.OnConflictStrategy.IGNORE;

public abstract class BaseDao<T extends BaseEntity> {

    /**
     * By using IGNORE as onConflict value is the return value -1 on a failure.
     * By using REPLACE as onConflict value is the existing row dropped and then inserted which triggers foreign key actions.
     *
     * @param entity which should be persisted
     * @return id to access entity
     */
    @Insert( onConflict = IGNORE )
    protected abstract long insertInternal(T entity );

    /**
     * @param entity to persist
     * @return Same entity with a new id
     */
    public T insert(final T entity )
    {
        final long id = insertInternal( entity );
        entity.setId( id );
        return entity;
    }

    /**
     * @param entity which should be persisted
     * @return number of updated entities
     */
    @Update( onConflict = IGNORE)
    public abstract int update( T entity );

    @Delete
    public abstract int delete( T entity );

}
