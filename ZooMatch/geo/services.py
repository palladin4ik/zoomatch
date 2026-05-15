import requests

from django.conf import settings


GEOCODER_URL = 'https://geocode-maps.yandex.ru/v1'


def get_feature_member(address):
    params = {
        'apikey': settings.YANDEX_MAPS_API_KEY,
        'geocode': address,
        'lang': 'ru_RU',
        'format': 'json',
    }

    response = requests.get(
        GEOCODER_URL,
        params=params,
        timeout=10,
    )

    response.raise_for_status()

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
