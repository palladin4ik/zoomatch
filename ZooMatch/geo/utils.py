from math import radians, cos, sin, asin, sqrt


EARTH_RADIUS_KM = 6371


def build_location(latitude, longitude):
    return f'{latitude},{longitude}'


def parse_location(location):
    if not location:
        return None

    try:
        latitude, longitude = location.split(',')

        return (
            float(latitude),
            float(longitude),
        )

    except (ValueError, AttributeError):
        return None


def haversine_distance(lat1, lon1, lat2, lon2):
    """
    Возвращает расстояние между двумя
    координатами в киллометрах
    """
    lat1 = radians(lat1)
    lon1 = radians(lon1)

    lat2 = radians(lat2)
    lon2 = radians(lon2)

    delta_lat = lat2 - lat1
    delta_lon = lon2 - lon1

    haversine_formula = (
        sin(delta_lat / 2) ** 2 + cos(lat1) * cos(lat2) *
        sin(delta_lon / 2) ** 2
        )

    angular_distance = 2 * asin(sqrt(haversine_formula))

    return EARTH_RADIUS_KM * angular_distance
