package dev.miaxion.bridge.extension.api;

/** Runs on the declared game thread; implementations must check context and never force-load chunks. */
@FunctionalInterface
public interface ExtensionHandler {
    String invoke(ExtensionInvocation context, String argumentsJson) throws Exception;
}
