# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Version 6.0.0 - 2025-06-11

### Added

- Support for the v2 `search` (forward geocoding) endpoint! The new API includes better structure, more details, and better address formatting.

- val res = service.reverse(59.444351, 24.750645).execute()
+ val res = service.reverseV2(59.444351, 24.750645).execute()
```diff
- val res = service.search("Telliskivi 60a/3, Tallinn, Estonia").execute()
+ val res = service.searchV2("Telliskivi 60a/3, Tallinn, Estonia").execute()
```

For an overview of the structural changes we've made in the V2 API,
refer to the [migration guide](https://docs.stadiamaps.com/geocoding-search-autocomplete/v2-api-migration-guide/).

### Fixed

- **Potentially breaking change:** The `maneuvers` property on route responses was previously marked as required.
  However, it is possible to explicitly request routes with this field removed.
  These would fail validation and the whole request would end with an exception
  in the API client.
  This has been fixed in this version, so the property is optional.

## Version 5.3.0 - 2025-06-03

### Added

- New fields to the time zone API responses including localized timestamps in several standard formats.

### Fixed

- Fix a bug which caused structured search bulk requests to incorrectly spell the `postalcode` field.

## Version 5.2.0 - 2023-04-21

# Added

- Add documentation for the geocoding metadata `query` field.

## Fixed

- Removed boundary circle properties that were mistakenly added.
  They did not behave as expected, so this is a bug fix despite being a code-breaking change if you used it.
- Added missing water layers to context.

## Version 5.1.0 - 2025-04-19

### Added

* Support for the v2 reverse geocoding endpoint! The new API includes better structure, more details, and better address formatting.

```diff
- val res = service.reverse(59.444351, 24.750645).execute()
+ val res = service.reverseV2(59.444351, 24.750645).execute()
```

For an overview of the structural changes we've made in the V2 API,
refer to the [migration guide](https://docs.stadiamaps.com/geocoding-search-autocomplete/v2-api-migration-guide/).

### Fixed

* Added the `wheelchair` property to the OSM addendum model (it was in the API response, but not explicitly modeled).
* Fix the types of the Natural Earth and Karmashapes identifiers

## Version 5.0.0 - 2025-04-07

### Added

- Support for the v2 autocomplete and place details APIs!
- **BREAKING:** We have renamed the Place Details method to clarify its purpose.

If you want to keep using the v1 endpoint, you can amend your code like so:

```diff
- val res = service.place(CollectionFormats.CSVParams("openstreetmap:address:way/109867749")).execute()
+ val res = service.placeDetails(CollectionFormats.CSVParams("openstreetmap:address:way/109867749")).execute()
```

To upgrade to the v2 Place Details endpoint, which features improved address formatting,
use the new V2 method:

```diff
- val res = service.place(CollectionFormats.CSVParams("openstreetmap:address:way/109867749")).execute()
+ val res = service.placeDetailsV2(CollectionFormats.CSVParams("openstreetmap:address:way/109867749")).execute()
```

We are also changing the `layer` property to a string.
While layer identifiers remain strongly typed at request time,
we have relaxed the schema type here to allow new layers in the future without breaking existing clients
(Kotlin, Python, and others will crash when they see an unknown variant in enum mode).

You will receive errors for all breaking changes at build time, so there should not be any hidden surprises.
We expect the upgrade to take less than 5 minutes.

For an overview of the structural changes we've made in the V2 API,
refer to the [migration guide](https://docs.stadiamaps.com/geocoding-search-autocomplete/v2-api-migration-guide/).

### Fixed

- The v1 geocoding model now includes the confidence score. This was always available in the API response, but wasn't in the OpenAPI spec.

## Version 4.0.0 - 2025-01-27

### Added

- Adds support for the OSRM format and navigation aids.
- Explicitly documented more properties on the geocoding feature model.
- Adds support for the `foursquare` data source.
- Documents the elevation interval parameter on certain routing requests.

### Changed

- BREAKING: This unfortunately means that some properties of the route response model, due to how the OpenAPI generator handles these models (it does not know how to generate variant models yet).
- BREAKING: Renamed models containing Valhalla and Pelias in their names to be generic. These now have rout(e|ing) or geocod(e|ing) prefixes.
- BREAKING: Removed one layer of nesting in the API namespace.
- Switched to [OpenAPI Generator](https://openapi-generator.tech/docs/generators/kotlin/) as the previous plugin appears to be abandoned.

### Migration notes

#### Imports

API imports have changed slightly, removing one level of nesting.
Simply remove the `apis` package from the path like so:

```diff
- import com.stadiamaps.api.apis.GeocodingApi
+ import com.stadiamaps.api.GeocodingApi
```

#### Renamed types

Several geocoding and routing types have been renamed to reflect their purpose better.
All types beginning with `Pelias` (e.g. `PeliasLayer`) now have a `Geocoding` prefix (e.g. `GeocodingLayer`).
Similarly, all types with a `Valhalla` prefix (e.g. `ValhallaLanguage`) now have a `Routing` prefix (e.g. `RoutingLanguage`). 

#### Routing API model changes

Some properties of `Route200Response` are now optional.
This is due to a bug in the OpenAPI generator for Kotlin,
which coalesces all properties into a single model rather than having two variations on the model based on format.
When requesting a route with navigation aids (`format = RouteRequest.Format.osrm`),
the `routes` property will contain the enhanced route information.
Existing code using the original compact format will continue using the `trip` property.
When you receive a successful response, your code can safely use the nun-null assertion operator (`!!`).

## Version 3.2.1 - 2024-08-15

### Fixed

- Fix issue with Kotlin bulk geocoding models

## Version 3.2.0 - 2024-08-15

### Added

- Add support for bulk geocoding

### Fixed

- Isochrone request models now support all costing models

## Version 3.1.0 - 2024-05-11

### Added

- Add support for elevation in route responses

## Version 3.0.0 - 2024-04-30

### Added

- Add support for low-speed vehicle routing
- The matrix endpoint now accepts its own model rather than coordinate. This includes a search cutoff and paves the way for future expansion.

### Changed

- Improved the documentation of the matrix endpoint failure modes

### Fixed

- The time and distance field on matrix source to target models are now marked as nullable

## Version 2.1.0 - 2024-03-21

### Added

- `ignore_` options for ignoring various restrictions (useful for certain map matching applications)

## Version 2.0.0 - 2024-03-19

### Changed

- BREAKING: Directions Options are moved from a nested object to the root of all turn-by-turn directions APIs. Simply remove the nesting.
- FIXED: Reflect upstream changes to the time/distance matrix API returning a single dimensional list of sources and targets; the extra layer of nesting is removed and may break existing code (this was a bug fix).
- Improved documentation strings.

### Added

- Alley factor for auto costing
- Resample distance parameter to height (elevation) requests
- Support for requesting alternate routes

## Version 1.0.1 - 2023-08-01

### Changed

- Missing cases to the travel type enum

## Version 1.0.0 - 2023-06-27

Initial release!
