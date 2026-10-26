using System.Numerics;
using System.Globalization;
using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

public sealed record RecipeResourceDto
{
    [JsonConstructor]
    public RecipeResourceDto(string kind, string resourceId, string ingredientTypeUid, string? displayName = null,
        string? numerator = null, string? denominator = null, string? unit = null)
    {
        RecipeContractValidation.RequireResourceLocation(resourceId, nameof(resourceId));
        if (kind is not ("item" or "fluid" or "chemical" or "energy" or "custom")) throw new ArgumentException("Invalid resource kind.", nameof(kind));
        if (string.IsNullOrWhiteSpace(ingredientTypeUid) || ingredientTypeUid.Length > 512 || ingredientTypeUid.Any(char.IsControl)) throw new ArgumentException("Invalid ingredient type.", nameof(ingredientTypeUid));
        if (displayName is not null && (displayName.Length > 1024 || displayName.Any(char.IsControl))) throw new ArgumentException("Invalid display name.", nameof(displayName));
        if ((numerator is null) != (denominator is null)) throw new ArgumentException("Incomplete resource amount.", nameof(numerator));
        if (numerator is not null && (numerator.Length is < 1 or > 128 || denominator!.Length is < 1 or > 128 ||
            !numerator.All(char.IsAsciiDigit) || !denominator.All(char.IsAsciiDigit) ||
            BigInteger.Parse(numerator, CultureInfo.InvariantCulture) <= 0 || BigInteger.Parse(denominator, CultureInfo.InvariantCulture) <= 0)) throw new ArgumentException("Invalid exact amount.", nameof(numerator));
        if (unit is not null && (string.IsNullOrWhiteSpace(unit) || unit.Length > 512 || unit.Any(char.IsControl))) throw new ArgumentException("Invalid unit.", nameof(unit));
        Kind = kind; ResourceId = resourceId; IngredientTypeUid = ingredientTypeUid; DisplayName = displayName;
        Numerator = numerator; Denominator = denominator; Unit = unit;
    }
    public string Kind { get; }
    public string ResourceId { get; }
    public string IngredientTypeUid { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] public string? DisplayName { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] public string? Numerator { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] public string? Denominator { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] public string? Unit { get; }
}
