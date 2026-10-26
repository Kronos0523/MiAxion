using System.Text.Json.Serialization;
using System.Text.RegularExpressions;
using MiAxion.Protocol.Serialization;

namespace MiAxion.Protocol.Contracts;

[JsonConverter(typeof(ProtocolActionJsonConverter))]
public readonly record struct ProtocolAction
{
    private const int MinLength = 3;
    private const int MaxLength = 64;
    private static readonly Regex WirePattern = new(
        "^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)+$",
        RegexOptions.CultureInvariant | RegexOptions.NonBacktracking);

    public ProtocolAction(string value)
    {
        ArgumentNullException.ThrowIfNull(value);

        if (value.Length is < MinLength or > MaxLength ||
            !WirePattern.IsMatch(value))
        {
            throw new ArgumentException(
                "Protocol action must be a lowercase ASCII dotted name.",
                nameof(value));
        }

        Value = value;
    }

    public string Value { get; }

    public bool TryGetKnown(out KnownProtocolAction known)
        => KnownProtocolActionWire.TryParse(Value, out known);

    public override string ToString() => Value;
}
