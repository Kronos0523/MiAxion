namespace MiAxion.Protocol.Contracts;

public sealed record ActionConfigurationRequest(int SchemaVersion);
public sealed record ActionConfigurationField(string Id, string Type, string Label, bool Required, bool ReadOnly, string StorageScope);
public sealed record ActionConfigurationGroup(string Id, string Label, IReadOnlyList<ActionConfigurationField> Fields);
public sealed record ActionConfigurationPage(int SchemaVersion, string PageId, string DisplayName, string ActionKind,
    IReadOnlyList<string> SupportedModes, IReadOnlyList<string> RequiredCapabilities, IReadOnlyList<ActionConfigurationGroup> Groups);
public sealed record ActionConfigurationEntry(string AddonId, string AddonVersion, string TargetModId, ActionConfigurationPage? Page, string? UnavailableReason);
public sealed record ActionConfigurationResponse(int SchemaVersion, string InstanceDirectory, IReadOnlyList<ActionConfigurationEntry> Entries);
