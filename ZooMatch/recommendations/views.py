from datetime import timedelta
from django.utils import timezone

from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response

from drf_spectacular.utils import (extend_schema, extend_schema_view,
                                   OpenApiParameter, OpenApiResponse,
                                   OpenApiTypes)

from .serializers import RecommendationParamsSerializer
from .pipeline import run_recommendation_pipeline
from .models import RecommendationCache, RecommendationParams

from pets.models import Pet
from pets.serializers_readonly import PetShortSerializer


CACHE_TTL_HOURS = 24


@extend_schema_view(
    recommend=extend_schema(
        summary='Получить рекомендации питомцев',
        description=(
            'Возвращает список питомцев подходящих для вязки с указанным '
            'питомцем.\n\n'
            'Система работает в гибридном режиме:\n'
            '- Если кэш актуален (не старше 24 часов), то возвращается '
            'кэшированный результат мгновенно\n'
            '- Если кэша нет, запускается полный пайплайн рекомендаций '
            'из 5 фаз\n\n'
            'Параметры поиска сохраняются и используются для ночного '
            'пересчёта рекомендаций\n\n'
        ),
        parameters=[
            OpenApiParameter(
                name='pet_id',
                description='ID питомца для которого ищем пару. '
                            'Питомец должен принадлежать текущему '
                            'пользователю.',
                required=True,
                type=OpenApiTypes.INT,
            ),
            OpenApiParameter(
                name='radius_km',
                description='Радиус поиска в км. '
                            'Допустимые значения: 50, 150, 300. '
                            'Если не указан — поиск без ограничения '
                            'по расстоянию.',
                required=False,
                type=OpenApiTypes.INT,
            ),
            OpenApiParameter(
                name='requires_pedigree',
                description='Фильтр по наличию родословной. '
                            'По умолчанию false.',
                required=False,
                type=OpenApiTypes.BOOL,
            ),
            OpenApiParameter(
                name='min_age',
                description='Минимальный возраст партнёра в годах.',
                required=False,
                type=OpenApiTypes.INT,
            ),
            OpenApiParameter(
                name='max_age',
                description='Максимальный возраст партнёра в годах.',
                required=False,
                type=OpenApiTypes.INT,
            ),
            OpenApiParameter(
                name='max_months_since_mating',
                description='Максимальное количество месяцев с '
                            'последней вязки партнёра.',
                required=False,
                type=OpenApiTypes.INT,
            ),
        ],
        responses={
            200: OpenApiResponse(
                description='Список рекомендованных питомцев',
                response={
                    'type': 'object',
                    'properties': {
                        'results': {
                            'type': 'array',
                            'items': {
                                'type': 'object'
                            },
                            'description': 'Список питомцев, '
                                           'отсортированный по релевантности'
                        },
                        'suggest_expand': {
                            'type': 'boolean',
                            'description': 'True если результатов мало '
                                           'и стоит расширить параметры '
                                           'поиска'
                        }
                    }
                }
            ),
        }
    )
)
class RecommendationViewSet(viewsets.GenericViewSet):
    permission_classes = [permissions.IsAuthenticated]

    @action(detail=False, methods=['get'])
    def recommend(self, request):
        params_serializer = RecommendationParamsSerializer(
            data=request.query_params
        )

        if not params_serializer.is_valid():
            return Response(
                params_serializer.errors,
                status=status.HTTP_400_BAD_REQUEST
            )

        params = params_serializer.validated_data
        pet_id = params.pop('pet_id')

        try:
            active_pet = Pet.objects.select_related(
                'animal_type', 'breed'
            ).get(id=pet_id, owner=request.user)
        except Pet.DoesNotExist:
            return Response(
                {'detail': 'Питомец не найден'},
                status=status.HTTP_404_NOT_FOUND
            )

        RecommendationParams.objects.update_or_create(
            pet=active_pet,
            defaults={
                'radius_km': params.get('radius_km', None),
                'requires_pedigree': params.get('requires_pedigree', False),
                'min_age': params.get('min_age', None),
                'max_age': params.get('max_age', None),
                'max_months_since_mating': params.get(
                    'max_months_since_mating', None
                ),
            }
        )

        cache_expiry = timezone.now() - timedelta(hours=CACHE_TTL_HOURS)
        cache = (
            RecommendationCache.objects
            .filter(pet=active_pet, updated_at__gte=cache_expiry)
            .first()
        )

        if cache:
            pets = Pet.objects.filter(
                id__in=cache.candidate_ids
            ).select_related('animal_type', 'breed', 'owner')

            pets_map = {pet.id: pet for pet in pets}
            ordered_pets = [pets_map[pid] for pid in cache.candidate_ids]

            return Response({
                'results': PetShortSerializer(ordered_pets, many=True).data,
                'suggest_expand': False,
            })

        result = run_recommendation_pipeline(active_pet, params)

        if result['results']:
            RecommendationCache.objects.update_or_create(
                pet=active_pet,
                defaults={
                    'candidate_ids': [p.id for p in result['results']]
                },
            )

        return Response({
            'results': PetShortSerializer(result['results'], many=True).data,
            'suggest_expand': result['suggest_expand']
        })
