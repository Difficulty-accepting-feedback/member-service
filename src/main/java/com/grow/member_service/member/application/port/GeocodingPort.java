package com.grow.member_service.member.application.port;

public interface GeocodingPort {
	record LatLng(double lat, double lng) {}
	LatLng geocodeRegion(String regionText);
}