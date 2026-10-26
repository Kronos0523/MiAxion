namespace MiAxion.Protocol.Contracts;

internal static class EntityTextValidation
{
    public static void Require(string value, string parameterName)
    {
        for (var index = 0; index < value.Length; index++)
        {
            var current = value[index];
            if (char.IsHighSurrogate(current))
            {
                if (index + 1 >= value.Length || !char.IsLowSurrogate(value[index + 1]))
                    throw new ArgumentException(
                        $"{parameterName} contains an invalid Unicode scalar.",
                        parameterName);
                index++;
                continue;
            }

            if (char.IsLowSurrogate(current))
                throw new ArgumentException(
                    $"{parameterName} contains an invalid Unicode scalar.",
                    parameterName);

            var codePoint = (int)current;
            if (codePoint <= 0x1F || codePoint is >= 0x7F and <= 0x9F ||
                codePoint is 0x061C or 0x200E or 0x200F ||
                codePoint is >= 0x202A and <= 0x202E ||
                codePoint is >= 0x2066 and <= 0x2069)
            {
                throw new ArgumentException(
                    $"{parameterName} contains a forbidden control character.",
                    parameterName);
            }
        }
    }
}
