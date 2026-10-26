using System.Text;

namespace MiAxion.Protocol.Tests;

internal static class StrictUtf8
{
    private static readonly byte[] Utf8Bom = [0xEF, 0xBB, 0xBF];
    private static readonly UTF8Encoding Decoder = new(
        encoderShouldEmitUTF8Identifier: false,
        throwOnInvalidBytes: true);

    internal static string ReadAllText(string path)
    {
        var bytes = File.ReadAllBytes(path);
        return Decode(bytes, path);
    }

    internal static string Decode(
        ReadOnlySpan<byte> bytes,
        string source)
    {
        if (bytes.StartsWith(Utf8Bom))
        {
            throw new InvalidDataException(
                $"UTF-8 BOM is forbidden: {source}");
        }

        string text;
        try
        {
            text = Decoder.GetString(bytes);
        }
        catch (DecoderFallbackException exception)
        {
            throw new InvalidDataException(
                $"Invalid UTF-8: {source}",
                exception);
        }

        if (text.Contains('\r', StringComparison.Ordinal))
        {
            throw new InvalidDataException(
                $"Only LF line endings are allowed: {source}");
        }

        if (!text.EndsWith('\n') ||
            (text.Length > 1 && text[^2] == '\n'))
        {
            throw new InvalidDataException(
                $"File must end with exactly one LF: {source}");
        }

        return text;
    }
}
