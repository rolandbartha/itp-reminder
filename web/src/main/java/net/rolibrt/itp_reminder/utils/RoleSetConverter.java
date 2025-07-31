package net.rolibrt.itp_reminder.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.rolibrt.itp_reminder.models.Role;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class RoleSetConverter implements AttributeConverter<List<Role>, String> {

    @Override
    public String convertToDatabaseColumn(List<Role> attribute) {
        if (attribute == null || attribute.isEmpty()) return "";
        return attribute.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    @Override
    public List<Role> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return List.of();
        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(Role::valueOf)
                .collect(Collectors.toList());
    }
}