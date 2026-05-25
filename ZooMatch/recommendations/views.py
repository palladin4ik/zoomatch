from datetime import timedelta
from django.utils import timezone

from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response

from .serializers import RecommendationParamsSerializer
from .pipeline import run_recommendation_pipeline
from .models import RecommendationCache

from pets.models import Pet
from pets.serializers_readonly import PetShortSerializer


CACHE_TTL_HOURS = 24


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
            'suggested_expand': result['suggest_expand']
        })
