from rest_framework import permissions, viewsets, status
from rest_framework.response import Response
from rest_framework.decorators import action

from .services import geocode, reverse_geocode, get_pets_by_distance_circles
from .utils import haversine_distance
from pets.serializers_readonly import PetShortSerializer


class GeoViewSet(viewsets.GenericViewSet):
    permission_classes = [permissions.IsAuthenticated]

    @action(detail=False, methods=['get'], url_path='geocode')
    def geocode_view(self, request):
        address = request.query_params.get('address')

        if not address:
            return Response(
                {'detail': 'Адрес обязательный параметр'},
                status=status.HTTP_400_BAD_REQUEST,
            )

        result = geocode(address)

        if not result:
            return Response(
                {'detail': 'Адрес не найден'},
                status=status.HTTP_404_NOT_FOUND,
            )

        return Response(result, status=status.HTTP_200_OK)

    @action(detail=False, methods=['get'])
    def reverse(self, request):
        latitude = request.query_params.get('latitude')
        longitude = request.query_params.get('longitude')

        if not latitude or not longitude:
            return Response(
                {'detail': 'Долгота и широта обязательны'},
                status=status.HTTP_400_BAD_REQUEST,
            )

        try:
            latitude = float(latitude)
            longitude = float(longitude)
        except ValueError:
            return Response(
                {'detail': 'Координаты должны быть числами'},
                status=status.HTTP_400_BAD_REQUEST,
            )

        result = reverse_geocode(latitude, longitude)

        if not result:
            return Response(
                {'detail': 'Адрес не найден'},
                status=status.HTTP_404_NOT_FOUND,
            )

        return Response({'address': result}, status=status.HTTP_200_OK)

    @action(detail=False, methods=['get'])
    def distance(self, request):
        lat1 = request.query_params.get('lat1')
        lon1 = request.query_params.get('lon1')

        lat2 = request.query_params.get('lat2')
        lon2 = request.query_params.get('lon2')

        if not all([lat1, lon1, lat2, lon2]):
            return Response(
                {'detail': 'Все координаты обязательны'},
                status=status.HTTP_400_BAD_REQUEST,
            )

        try:
            lat1 = float(lat1)
            lon1 = float(lon1)

            lat2 = float(lat2)
            lon2 = float(lon2)
        except ValueError:
            return Response(
                {'detail': 'Координаты должны быть числами'},
                status=status.HTTP_400_BAD_REQUEST,
            )

        distance = haversine_distance(
            lat1,
            lon1,
            lat2,
            lon2
        )

        return Response({'distance_km': round(distance, 2)},
                        status=status.HTTP_200_OK)

    @action(detail=False, methods=['get'])
    def pets_circles(self, request):
        latitude = request.query_params.get('latitude')
        longitude = request.query_params.get('longitude')

        if not latitude or not longitude:
            return Response(
                {'detail': 'Долгота и широта обязательны'},
                status=status.HTTP_400_BAD_REQUEST,
            )

        try:
            latitude = float(latitude)
            longitude = float(longitude)

        except ValueError:
            return Response(
                {'detail': 'Координаты должны быть числами'},
                status=status.HTTP_400_BAD_REQUEST,
            )

        result = get_pets_by_distance_circles(latitude, longitude)

        serialized_result = {}

        for circle_name, pets in result.items():
            serialized_result[circle_name] = PetShortSerializer(
                pets, many=True
            ).data

        return Response(serialized_result)
