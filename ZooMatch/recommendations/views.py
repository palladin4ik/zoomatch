from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response

from .serializers import RecommendationParamsSerializer
from .pipeline import run_recommendation_pipeline

from pets.models import Pet
from pets.serializers_readonly import PetShortSerializer


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

        result = run_recommendation_pipeline(active_pet, params)

        return Response({
            'results': PetShortSerializer(result['results'], many=True).data,
            'suggested_expand': result['suggest_expand']
        })
