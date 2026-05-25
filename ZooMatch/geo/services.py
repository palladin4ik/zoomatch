import requests

from django.conf import settings
from rest_framework import serializers

from .utils import build_location, parse_location, haversine_distance
from pets.models import Pet


def get_feature_member(address):
    GEOCODER_URL = 'https://geocode-maps.yandex.ru/v1'

    params = {
        'apikey': settings.YANDEX_MAPS_API_KEY,
        'geocode': address,
        'lang': 'ru_RU',
        'format': 'json',
    }

    try:
        response = requests.get(
            GEOCODER_URL,
            params=params,
            timeout=10,
        )

        response.raise_for_status()
    except requests.RequestException:
        return None

    data = response.json()

    return (
        data['response']['GeoObjectCollection']
        ['featureMember']
    )


def geocode(address):
    feature_members = get_feature_member(address)

    if not feature_members:
        return None

    geo_object = feature_members[0]['GeoObject']

    pos = geo_object['Point']['pos']

    longitude, latitude = pos.split()

    return {
        'latitude': float(latitude),
        'longitude': float(longitude)
    }


def reverse_geocode(latitude, longitude):
    feature_members = get_feature_member(
        f'{longitude},{latitude}'
    )

    if not feature_members:
        return None

    return (
        feature_members[0]['GeoObject']['metaDataProperty']
        ['GeocoderMetaData']['text']
    )


def get_pets_by_distance_circles(latitude, longitude):
    pets = (
        Pet.objects
        .filter(is_active=True)
        .select_related('animal_type', 'breed')
        .exclude(location__isnull=True)
        .exclude(location='')
    )

    circles = {
        'less_than_50': [],
        'less_than_150': [],
        'less_than_300': [],
        'more_than_300': [],
    }

    for pet in pets:
        parsed_location = parse_location(pet.location)

        if not parsed_location:
            continue

        pet_latitude, pet_longitude = parsed_location

        distance = haversine_distance(
            latitude,
            longitude,
            pet_latitude,
            pet_longitude,
        )

        pet.distance_km = distance

        if distance < 50:
            circles['less_than_50'].append(pet)
        elif distance < 150:
            circles['less_than_150'].append(pet)
        elif distance < 300:
            circles['less_than_300'].append(pet)
        else:
            circles['more_than_300'].append(pet)

    return circles


def build_location_from_input(address=None, latitude=None, longitude=None):
    if address:
        geocoded = geocode(address)

        if not geocoded:
            raise serializers.ValidationError(
                {'address': 'Адрес не найден'}
            )

        latitude = geocoded['latitude']
        longitude = geocoded['longitude']

        if not latitude or not longitude:
            raise serializers.ValidationError(
                {'location': 'Укажите адрес или координаты'}
            )

    return build_location(latitude, longitude)
