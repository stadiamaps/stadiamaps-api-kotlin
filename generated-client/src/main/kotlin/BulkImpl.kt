package com.stadiamaps.api.models

abstract class BulkRequestFactory {
    companion object {
        fun searchRequest(query: SearchQuery) =
            BulkRequest(
                BulkRequest.Endpoint.SlashV1SlashSearch, BulkRequestQuery(
                    text = query.text,
                    focusPointLat = query.focusPointLat,
                    focusPointLon = query.focusPointLon,
                    boundaryRectMinLat = query.boundaryRectMinLat,
                    boundaryRectMaxLat = query.boundaryRectMaxLat,
                    boundaryRectMinLon = query.boundaryRectMinLon,
                    boundaryRectMaxLon = query.boundaryRectMaxLon,
                    boundaryCircleLat = query.boundaryCircleLat,
                    boundaryCircleLon = query.boundaryCircleLon,
                    boundaryCircleRadius = query.boundaryCircleRadius,
                    boundaryCountry = query.boundaryCountry,
                    boundaryGid = query.boundaryGid,
                    layers = query.layers,
                    sources = query.sources,
                    propertySize = query.propertySize,
                    lang = query.lang,
                )
            )

        fun searchStructuredRequest(query: SearchStructuredQuery) =
            BulkRequest(
                BulkRequest.Endpoint.SlashV1SlashSearchSlashStructured, BulkRequestQuery(
                    address = query.address,
                    neighbourhood = query.neighbourhood,
                    borough = query.borough,
                    locality = query.locality,
                    region = query.region,
                    postalCode = query.postalCode,
                    country = query.country,
                    focusPointLat = query.focusPointLat,
                    focusPointLon = query.focusPointLon,
                    boundaryRectMinLat = query.boundaryRectMinLat,
                    boundaryRectMaxLat = query.boundaryRectMaxLat,
                    boundaryRectMinLon = query.boundaryRectMinLon,
                    boundaryRectMaxLon = query.boundaryRectMaxLon,
                    boundaryCircleLat = query.boundaryCircleLat,
                    boundaryCircleLon = query.boundaryCircleLon,
                    boundaryCircleRadius = query.boundaryCircleRadius,
                    boundaryCountry = query.boundaryCountry,
                    boundaryGid = query.boundaryGid,
                    layers = query.layers,
                    sources = query.sources,
                    propertySize = query.propertySize,
                    lang = query.lang,
                )
            )
    }
}
