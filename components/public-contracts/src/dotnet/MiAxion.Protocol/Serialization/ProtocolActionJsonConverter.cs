using System.Text.Json;
using System.Text.Json.Serialization;
using MiAxion.Protocol.Contracts;

namespace MiAxion.Protocol.Serialization;

public sealed class ProtocolActionJsonConverter
    : JsonConverter<ProtocolAction>
{
    public override ProtocolAction Read(
        ref Utf8JsonReader reader,
        Type typeToConvert,
        JsonSerializerOptions options)
    {
        if (reader.TokenType != JsonTokenType.String)
        {
            throw new JsonException("Protocol action must be a string.");
        }

        var value = reader.GetString();

        try
        {
            return new ProtocolAction(value!);
        }
        catch (ArgumentException exception)
        {
            throw new JsonException(
                "Protocol action has invalid wire syntax.",
                exception);
        }
    }

    public override void Write(
        Utf8JsonWriter writer,
        ProtocolAction value,
        JsonSerializerOptions options)
    {
        if (value.Value is null)
        {
            throw new JsonException(
                "The default ProtocolAction value cannot be serialized.");
        }

        writer.WriteStringValue(value.Value);
    }
}
