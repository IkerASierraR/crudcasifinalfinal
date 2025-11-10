package model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import model.TipoContrato;

@Converter(autoApply = true)
public class TipoContratoConverter implements AttributeConverter<TipoContrato, String> {

    @Override
    public String convertToDatabaseColumn(TipoContrato attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDescripcion();
    }

    @Override
    public TipoContrato convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return TipoContrato.NO_ESPECIFICADO;
        }
        return TipoContrato.fromDescripcion(dbData.trim());
    }
}