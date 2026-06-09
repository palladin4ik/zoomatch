from rest_framework import permissions, viewsets, status
from rest_framework.response import Response
from rest_framework.decorators import action

from drf_spectacular.utils import (extend_schema, OpenApiParameter,
                                   OpenApiResponse, OpenApiTypes)

from .serializers import PetsCirclesResponseSerializer
from .services import geocode, reverse_geocode, get_pets_by_distance_circles
from .utils import haversine_distance
from pets.serializers_readonly import PetShortSerializer


class GeoViewSet(viewsets.GenericViewSet):
    permission_classes = [permissions.IsAuthenticated]

    @extend_schema(
            summary='Геокодирование',
            description='Возвращает координаты заданного адреса',
            parameters=[
                OpenApiParameter(
                    name='address',
                    required=True,
                    type=OpenApiTypes.STR,
                )
            ],
            responses={
                200: OpenApiResponse(
                    response={
                        'type': 'object',
                        'properties': {
                            'latitude': {
                                'type': 'number',
                                'example': 55.7558,
                            },
                            'longitude': {
                                'type': 'number',
                                'example': 37.7558,
                            },
                        },
                    },
                    description='Координаты адреса',
                ),
                400: OpenApiResponse(
                    description='Адрес не передан',
                ),
                404: OpenApiResponse(
                    description='Адрес не найден',
                ),
            }
    )
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

    @extend_schema(
            summary='Обратное геокодирование',
            description='Возвращает адрес по переданым координатам',
            parameters=[
                OpenApiParameter(
                    name='latitude',
                    description='Долгота',
                    required=True,
                    type=OpenApiTypes.FLOAT,
                ),
                OpenApiParameter(
                    name='longitude',
                    description='Широта',
                    required=True,
                    type=OpenApiTypes.FLOAT,
                )
            ],
            responses={
                200: OpenApiResponse(
                    response={
                        'type': 'object',
                        'properties': {
                            'address': {
                                'type': 'string',
                                'example': 'Россия, Калуга, Московская улица 5'
                            },
                        },
                    },
                    description='Адрес'
                ),
                400: OpenApiResponse(
                    description='Долгота и широта обязательны и должны быть '
                    'числами'
                ),
                404: OpenApiResponse(
                    description='Адрес не найден'
                )
            }
    )
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

    @extend_schema(
            summary='Расчет дистанции между 2 точками',
            description='Возвращает дистанцию в км между двумя точками по '
            'формуле Хаверсина',
            parameters=[
                OpenApiParameter(
                    name='lat1',
                    description='Долгота 1 точки',
                    required=True,
                    type=OpenApiTypes.FLOAT,
                ),
                OpenApiParameter(
                    name='lon1',
                    description='Широта 1 точки',
                    required=True,
                    type=OpenApiTypes.FLOAT,
                ),
                OpenApiParameter(
                    name='lat2',
                    description='Долгота 2 точки',
                    required=True,
                    type=OpenApiTypes.FLOAT,
                ),
                OpenApiParameter(
                    name='lon2',
                    description='Широта 2 точки',
                    required=True,
                    type=OpenApiTypes.FLOAT,
                ),
            ],
            responses={
                200: OpenApiResponse(
                    response={
                        'type': 'object',
                        'properties': {
                            'distance_km': {
                                'type': 'number',
                                'example': '21.45',
                            },
                        },
                    },
                    description='Расстояние',
                ),
                400: OpenApiResponse(
                    description='Все координаты обязательны и должны '
                    'быть числами'
                )
            }
    )
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

    @extend_schema(
            summary='Получение ближайших питомцев по окружностям',
            description='Возвращает список ближайших питомцев по окружностям '
            'в < 50; < 150; < 300; 300+ км',
            parameters=[
                OpenApiParameter(
                    name='latitude',
                    description='Долгота',
                    required=True,
                    type=OpenApiTypes.FLOAT,
                ),
                OpenApiParameter(
                    name='longitude',
                    description='Широта',
                    required=True,
                    type=OpenApiTypes.FLOAT,
                )
            ],
            responses={
                200: OpenApiResponse(
                    response=PetsCirclesResponseSerializer,
                    description='Питомцы по окружностям'
                )
            }
    )
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
