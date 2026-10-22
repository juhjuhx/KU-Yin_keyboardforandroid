# Frozen P1-E instrumentation source

These eight files retain the exact bytes listed by the unchanged
`scripts/p1_instrumentation_manifest.json` (`fixture_source_sha=6595b684...`).
They are evidence, not compiled current instrumentation.

Reused byte-for-byte from the existing R2 source pack at
`d6bf2a68f68fe6ea0bd4bbbf79016a863abb29d1`; every SHA256 was checked against
the canonical manifest before writing. This is the same scoped preservation
approach already implemented on the R2 branch, not a replacement acceptance
framework. Missing or modified frozen bytes must fail, never skip.

Current Host test source may evolve in its normal module, with its assertions
reviewed separately. This archive does not prove that current tests ran or that
current APKs match frozen P1-E APKs. Original worktrees, ZIPs, manifests, and
runner pin remain unchanged.
