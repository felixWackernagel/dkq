package de.wackernagel.dkq.room.converter;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;
import de.wackernagel.dkq.room.entities.Message;

public class MessageTypeConverter {

    @TypeConverter
    public static Message.Type toType( final int value ) {
        return Message.Type.byCode( value );
    }

    @TypeConverter
    public static int toInt( @Nullable final Message.Type value ) {
        return value == null ? 0 : value.getCode();
    }

}
