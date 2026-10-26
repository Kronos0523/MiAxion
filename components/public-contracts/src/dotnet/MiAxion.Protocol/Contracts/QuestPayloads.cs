using System.Text;
using System.Text.Json.Serialization;
using System.Text.RegularExpressions;

namespace MiAxion.Protocol.Contracts;

public enum QuestQueryScope
{
    [JsonStringEnumMemberName("overview")]
    Overview,

    [JsonStringEnumMemberName("active")]
    Active,

    [JsonStringEnumMemberName("available")]
    Available,

    [JsonStringEnumMemberName("completed")]
    Completed,

    [JsonStringEnumMemberName("search")]
    Search
}

public enum QuestState
{
    [JsonStringEnumMemberName("locked")]
    Locked,

    [JsonStringEnumMemberName("available")]
    Available,

    [JsonStringEnumMemberName("active")]
    Active,

    [JsonStringEnumMemberName("completed")]
    Completed
}

public enum QuestDescriptionVisibility
{
    [JsonStringEnumMemberName("visible")]
    Visible,

    [JsonStringEnumMemberName("hidden_until_complete")]
    HiddenUntilComplete
}

public enum QuestRewardClaimState
{
    [JsonStringEnumMemberName("claimed")]
    Claimed,

    [JsonStringEnumMemberName("claimable")]
    Claimable,

    [JsonStringEnumMemberName("blocked")]
    Blocked
}

public sealed record QuestQueryRequest
{
    public QuestQueryRequest(QuestQueryScope scope, string? filter = null, string? continuation = null)
    {
        if (!Enum.IsDefined(scope)) throw new ArgumentOutOfRangeException(nameof(scope));
        QuestContractValidation.Continuation(continuation);
        if (scope == QuestQueryScope.Search)
            QuestContractValidation.RequireText(filter, 1, 256, nameof(filter));
        else if (filter is not null)
            throw new ArgumentException("Only search scope accepts a filter.", nameof(filter));

        Scope = scope;
        Filter = filter;
        Continuation = continuation;
    }

    public QuestQueryScope Scope { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? Filter { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? Continuation { get; }
}

public sealed record QuestDetailRequest
{
    public QuestDetailRequest(string questId)
    {
        QuestContractValidation.RequireOpaqueId(questId, nameof(questId));
        QuestId = questId;
    }

    public string QuestId { get; }
}

public sealed record QuestQueryEntry
{
    public QuestQueryEntry(
        string questId,
        string chapterTitle,
        string title,
        QuestState state,
        double progress,
        int taskCount,
        int completedTaskCount,
        bool repeatable)
    {
        QuestContractValidation.RequireOpaqueId(questId, nameof(questId));
        QuestContractValidation.RequireTitle(chapterTitle, nameof(chapterTitle));
        QuestContractValidation.RequireTitle(title, nameof(title));
        QuestContractValidation.RequireProgress(progress, nameof(progress));
        if (taskCount is < 0 or > ProtocolConstants.QuestDetailCollectionMaximum)
            throw new ArgumentOutOfRangeException(nameof(taskCount));
        if (completedTaskCount < 0 || completedTaskCount > taskCount)
            throw new ArgumentOutOfRangeException(nameof(completedTaskCount));

        QuestId = questId;
        ChapterTitle = chapterTitle;
        Title = title;
        State = state;
        Progress = progress;
        TaskCount = taskCount;
        CompletedTaskCount = completedTaskCount;
        Repeatable = repeatable;
    }

    public string QuestId { get; }
    public string ChapterTitle { get; }
    public string Title { get; }
    public QuestState State { get; }
    public double Progress { get; }
    public int TaskCount { get; }
    public int CompletedTaskCount { get; }
    public bool Repeatable { get; }
}

public sealed record QuestQueryResponse
{
    public QuestQueryResponse(
        IReadOnlyList<QuestQueryEntry> quests,
        int matchedCount,
        bool truncated,
        string? nextContinuation = null)
    {
        Quests = QuestContractValidation.CopyWithinBounds(
            quests, ProtocolConstants.QuestQueryResultMaximum, nameof(quests));
        ArgumentOutOfRangeException.ThrowIfLessThan(
            matchedCount, Quests.Count, nameof(matchedCount));
        if (truncated != (matchedCount > Quests.Count))
            throw new ArgumentException(
                "truncated must exactly describe omitted query matches.", nameof(truncated));

        MatchedCount = matchedCount;
        Truncated = truncated;
        QuestContractValidation.Continuation(nextContinuation);
        if (nextContinuation is not null && (!truncated || Quests.Count != ProtocolConstants.QuestQueryResultMaximum))
            throw new ArgumentException("A continuation requires a full non-final page.", nameof(nextContinuation));
        NextContinuation = nextContinuation;
    }

    public IReadOnlyList<QuestQueryEntry> Quests { get; }
    public int MatchedCount { get; }
    public bool Truncated { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? NextContinuation { get; }
}

public sealed record QuestObjective
{
    public QuestObjective(
        string taskId,
        string typeId,
        string title,
        long currentProgress,
        long maximumProgress,
        string formattedProgress,
        bool completed,
        bool optional)
    {
        QuestContractValidation.RequireOpaqueId(taskId, nameof(taskId));
        QuestContractValidation.RequireResourceLocation(typeId, nameof(typeId));
        QuestContractValidation.RequireTitle(title, nameof(title));
        QuestContractValidation.RequireTitle(formattedProgress, nameof(formattedProgress));
        if (maximumProgress is < 1 or > ProtocolConstants.QuestProgressIntegerMaximum)
            throw new ArgumentOutOfRangeException(nameof(maximumProgress));
        if (currentProgress < 0 || currentProgress > maximumProgress)
            throw new ArgumentOutOfRangeException(nameof(currentProgress));

        TaskId = taskId;
        TypeId = typeId;
        Title = title;
        CurrentProgress = currentProgress;
        MaximumProgress = maximumProgress;
        FormattedProgress = formattedProgress;
        Completed = completed;
        Optional = optional;
    }

    public string TaskId { get; }
    public string TypeId { get; }
    public string Title { get; }
    public long CurrentProgress { get; }
    public long MaximumProgress { get; }
    public string FormattedProgress { get; }
    public bool Completed { get; }
    public bool Optional { get; }
}

public sealed record QuestReward
{
    public QuestReward(
        string rewardId,
        string typeId,
        string title,
        bool teamReward,
        bool autoClaim,
        QuestRewardClaimState claimState)
    {
        QuestContractValidation.RequireOpaqueId(rewardId, nameof(rewardId));
        QuestContractValidation.RequireResourceLocation(typeId, nameof(typeId));
        QuestContractValidation.RequireTitle(title, nameof(title));
        RewardId = rewardId;
        TypeId = typeId;
        Title = title;
        TeamReward = teamReward;
        AutoClaim = autoClaim;
        ClaimState = claimState;
    }

    public string RewardId { get; }
    public string TypeId { get; }
    public string Title { get; }
    public bool TeamReward { get; }
    public bool AutoClaim { get; }
    public QuestRewardClaimState ClaimState { get; }
}

public sealed record QuestDependency
{
    public QuestDependency(string questId, string title)
    {
        QuestContractValidation.RequireOpaqueId(questId, nameof(questId));
        QuestContractValidation.RequireTitle(title, nameof(title));
        QuestId = questId;
        Title = title;
    }

    public string QuestId { get; }
    public string Title { get; }
}

public sealed record QuestDetailResponse
{
    public QuestDetailResponse(
        string questId,
        string chapterTitle,
        string title,
        string subtitle,
        QuestDescriptionVisibility descriptionVisibility,
        IReadOnlyList<string> description,
        QuestState state,
        double progress,
        bool repeatable,
        bool optional,
        IReadOnlyList<QuestObjective> objectives,
        IReadOnlyList<QuestReward> rewards,
        IReadOnlyList<QuestDependency> dependencies)
    {
        QuestContractValidation.RequireOpaqueId(questId, nameof(questId));
        QuestContractValidation.RequireTitle(chapterTitle, nameof(chapterTitle));
        QuestContractValidation.RequireTitle(title, nameof(title));
        QuestContractValidation.RequireTitle(subtitle, nameof(subtitle));
        QuestContractValidation.RequireProgress(progress, nameof(progress));

        Description = QuestContractValidation.CopyDescription(description, nameof(description));
        // An exact quest ID is not authority to reveal FTB text hidden from the player.
        if (descriptionVisibility == QuestDescriptionVisibility.HiddenUntilComplete &&
            Description.Count != 0)
            throw new ArgumentException(
                "Hidden description text cannot be transported.", nameof(description));

        QuestId = questId;
        ChapterTitle = chapterTitle;
        Title = title;
        Subtitle = subtitle;
        DescriptionVisibility = descriptionVisibility;
        State = state;
        Progress = progress;
        Repeatable = repeatable;
        Optional = optional;
        Objectives = QuestContractValidation.CopyWithinBounds(
            objectives, ProtocolConstants.QuestDetailCollectionMaximum, nameof(objectives));
        Rewards = QuestContractValidation.CopyWithinBounds(
            rewards, ProtocolConstants.QuestDetailCollectionMaximum, nameof(rewards));
        Dependencies = QuestContractValidation.CopyWithinBounds(
            dependencies, ProtocolConstants.QuestDetailCollectionMaximum, nameof(dependencies));
    }

    public string QuestId { get; }
    public string ChapterTitle { get; }
    public string Title { get; }
    public string Subtitle { get; }
    public QuestDescriptionVisibility DescriptionVisibility { get; }
    public IReadOnlyList<string> Description { get; }
    public QuestState State { get; }
    public double Progress { get; }
    public bool Repeatable { get; }
    public bool Optional { get; }
    public IReadOnlyList<QuestObjective> Objectives { get; }
    public IReadOnlyList<QuestReward> Rewards { get; }
    public IReadOnlyList<QuestDependency> Dependencies { get; }
}

internal static partial class QuestContractValidation
{
    internal static void Continuation(string? value)
    {
        if (value is not null && (value.Length is < 1 or > 256 ||
            !value.All(static c => c is >= 'A' and <= 'Z' or >= 'a' and <= 'z' or >= '0' and <= '9' or '_' or '-')))
            throw new ArgumentException("Quest continuation is invalid.", nameof(value));
    }

    [GeneratedRegex("^[A-Za-z0-9_.:-]+$", RegexOptions.CultureInvariant)]
    private static partial Regex OpaqueIdPattern();

    [GeneratedRegex("^[a-z0-9_.-]+:[a-z0-9_./-]+$", RegexOptions.CultureInvariant)]
    private static partial Regex ResourceLocationPattern();

    internal static void RequireOpaqueId(string value, string parameterName)
    {
        RequireText(value, 1, 64, parameterName);
        if (!OpaqueIdPattern().IsMatch(value))
            throw new ArgumentException("Value must be a safe opaque ID.", parameterName);
    }

    internal static void RequireResourceLocation(string value, string parameterName)
    {
        RequireText(value, 1, 128, parameterName);
        if (!ResourceLocationPattern().IsMatch(value))
            throw new ArgumentException("Value must be a resource location.", parameterName);
    }

    internal static void RequireTitle(string value, string parameterName) =>
        RequireText(value, 0, ProtocolConstants.QuestTitleScalarMaximum, parameterName);

    internal static void RequireText(
        string? value,
        int minimumScalars,
        int maximumScalars,
        string parameterName)
    {
        ArgumentNullException.ThrowIfNull(value, parameterName);
        var count = value.EnumerateRunes().Count();
        if (count < minimumScalars || count > maximumScalars)
            throw new ArgumentOutOfRangeException(parameterName);
    }

    internal static void RequireProgress(double value, string parameterName)
    {
        if (!double.IsFinite(value) || value is < 0 or > 1)
            throw new ArgumentOutOfRangeException(parameterName);
    }

    internal static IReadOnlyList<T> CopyWithinBounds<T>(
        IReadOnlyList<T> values,
        int maximum,
        string parameterName)
    {
        ArgumentNullException.ThrowIfNull(values, parameterName);
        if (values.Count > maximum)
            throw new ArgumentOutOfRangeException(parameterName);
        var copy = values.ToArray();
        if (copy.Any(value => value is null))
            throw new ArgumentException("Collection elements cannot be null.", parameterName);
        return Array.AsReadOnly(copy);
    }

    internal static IReadOnlyList<string> CopyDescription(
        IReadOnlyList<string> values,
        string parameterName)
    {
        var copy = CopyWithinBounds(
            values, ProtocolConstants.QuestDetailCollectionMaximum, parameterName);
        foreach (var line in copy)
            RequireText(line, 0, ProtocolConstants.QuestDescriptionLineScalarMaximum, parameterName);
        return copy;
    }
}
