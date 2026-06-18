# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [21.17.7] - 2026-05-15
### Fixed
- Fixed skillbook icons missing due to mod_id update

## [21.17.6] - 2026-05-15
### Changes
- Renamed `slash_modifier` to `damage_attribute_modifier` to improve clarity. Warning: would break datapacks.

## [21.17.5] - 2026-05-15
### Fixed
- Fixed damage modifier not being applied correctly

## [21.17.4] - 2026-05-15
### Fixed
- Fixed deserializer on DamageAttribute looking for a float which does not exist on json.

## [21.17.3] - 2026-05-14
### Fixed
- Fixed Battle Arts Custom Data not registering.


## [21.17.2] - 2026-05-14
### Added
- Damage Attributes, a new feature that regards animation-specific damage types. This is complemented by the `PhysicalDamageType` tag.

### Fixed
- Missing translation keys.