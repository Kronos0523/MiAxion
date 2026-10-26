using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

/// <summary>Only bounded scheduler counters; never a partial world result.</summary>
public sealed record WorldQueryProgressResponse
{
    [JsonConstructor]
    public WorldQueryProgressResponse(
        bool active,
        string phase,
        int completedUnits,
        int totalUnits,
        int examined,
        int loaded)
    {
        ArgumentNullException.ThrowIfNull(phase);
        if (phase.Length is < 1 or > 48 ||
            !System.Text.RegularExpressions.Regex.IsMatch(
                phase, "^[a-z][a-z0-9_.-]*$", System.Text.RegularExpressions.RegexOptions.CultureInvariant))
            throw new ArgumentException("Phase is outside its bounds.", nameof(phase));
        if (completedUnits < 0 || totalUnits < 0 || completedUnits > totalUnits ||
            completedUnits > ProtocolConstants.WorldQueryProgressMaximum ||
            totalUnits > ProtocolConstants.WorldQueryProgressMaximum ||
            examined < 0 || examined > ProtocolConstants.WorldQueryProgressMaximum ||
            loaded < 0 || loaded > totalUnits ||
            loaded > ProtocolConstants.WorldQueryProgressMaximum)
            throw new ArgumentOutOfRangeException(nameof(completedUnits));
        if (!active && (completedUnits != 0 || totalUnits != 0 || examined != 0 || loaded != 0))
            throw new ArgumentException("Inactive progress must not expose stale counters.", nameof(active));
        Active = active;
        Phase = phase;
        CompletedUnits = completedUnits;
        TotalUnits = totalUnits;
        Examined = examined;
        Loaded = loaded;
    }

    public bool Active { get; }
    public string Phase { get; }
    public int CompletedUnits { get; }
    public int TotalUnits { get; }
    public int Examined { get; }
    public int Loaded { get; }
}
