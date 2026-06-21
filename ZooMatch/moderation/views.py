from rest_framework import viewsets, permissions, mixins, status
from rest_framework.response import Response
from rest_framework.decorators import action
from django.utils import timezone

from .serializers import ModerationRequestSerializer
from .models import ModerationRequest

from pets.models import AnimalType, Breed, Pet


class ModerationRequestViewSet(mixins.ListModelMixin, viewsets.GenericViewSet):
    queryset = ModerationRequest.objects.filter(
        status=ModerationRequest.Status.PENDING
        )
    serializer_class = ModerationRequestSerializer
    permission_classes = [permissions.IsAdminUser]

    @action(detail=True, methods=['post'])
    def approve(self, request, pk=None):
        moderation_request = self.get_object()
        pet = moderation_request.pet

        if moderation_request.animal_type:
            new_animal_type, _ = AnimalType.objects.get_or_create(
                name=moderation_request.animal_type
                )
            pet.animal_type = new_animal_type

            if moderation_request.breed:
                new_breed = Breed.objects.create(
                    name=moderation_request.breed,
                    animal_type=new_animal_type
                    )
                pet.breed = new_breed

        elif moderation_request.breed:
            new_breed = Breed.objects.create(
                name=moderation_request.breed,
                animal_type=pet.animal_type
            )
            pet.breed = new_breed

        pet.moderation_status = Pet.ModerationStatus.APPROVED
        pet.save()

        moderation_request.reviewed_by = self.request.user
        moderation_request.reviewed_at = timezone.now()
        moderation_request.status = ModerationRequest.Status.APPROVED
        moderation_request.save()

        return Response({"status": "Заявка одобрена"},
                        status=status.HTTP_200_OK)

    @action(detail=True, methods=['post'])
    def reject(self, request, pk=None):
        moderation_request = self.get_object()
        pet = moderation_request.pet

        moderation_request.reviewed_by = self.request.user
        moderation_request.reviewed_at = timezone.now()
        moderation_request.status = ModerationRequest.Status.REJECTED
        moderation_request.save()

        pet.moderation_status = Pet.ModerationStatus.REJECTED
        pet.save()

        return Response(
            {"status": "Заявка отклонена"}, status=status.HTTP_200_OK)
