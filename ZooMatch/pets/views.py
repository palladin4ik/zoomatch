from rest_framework import viewsets, permissions, mixins
from rest_framework.response import Response
from rest_framework.decorators import action

from drf_spectacular.utils import extend_schema

from .serializer import (
    AnimalTypeSerializer, BreedSerializer,
    TagSerializer, PetSerializer,
    PetCreateUpdateSerializer,
    PetInfoSerializer, CommentSerializer
)
from .models import AnimalType, Breed, Tag, Pet, PetInfo, Comment
from .permissions import IsAdminOrReadOnly, IsOwnerOrReadOnly


class AnimalTypeViewSet(viewsets.ModelViewSet):
    queryset = AnimalType.objects.all()
    serializer_class = AnimalTypeSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdminOrReadOnly]


class BreedViewSet(viewsets.ModelViewSet):
    queryset = Breed.objects.all()
    serializer_class = BreedSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdminOrReadOnly]


class PetViewSet(viewsets.ModelViewSet):
    permission_classes = [permissions.IsAuthenticated, IsOwnerOrReadOnly]

    def get_queryset(self):
        user = self.request.user
        if self.action in ['update', 'partial_update', 'destroy']:
            return Pet.objects.filter(owner=user)
        if self.action == 'list':
            return Pet.objects.filter(is_active=True)
        return Pet.objects.all()

    def get_serializer_class(self):
        if self.action in ['create', 'update', 'partial_update']:
            return PetCreateUpdateSerializer
        return PetSerializer

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)

    @action(detail=False, methods=['get'])
    def me(self, request):
        queryset = Pet.objects.filter(owner=request.user)
        serializer = PetSerializer(queryset, many=True)
        return Response(serializer.data)
