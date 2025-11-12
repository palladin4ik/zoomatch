from rest_framework import viewsets, permissions
from rest_framework.response import Response
from rest_framework.decorators import action

from drf_spectacular.utils import extend_schema, extend_schema_view

from .serializer import (
    AnimalTypeSerializer, BreedSerializer,
    PetSerializer, PetCreateUpdateSerializer,
    PetInfoSerializer, CommentSerializer
)
from .models import AnimalType, Breed, Pet, PetInfo, Comment
from .permissions import IsAdminOrReadOnly, IsOwnerOrReadOnly


COMMON_PERMISSION_DESCRIPTION = '''
    Права доступа:
    - Просмотр доступен всем авторизованным пользователям
    - Создание, обновление, удаление - только администраторам (is_staff)
'''

COMMON_OWNER_PERMISSION = '''
    Права доступа:
    - Просмотр доступен всем авторизованным пользователям
    - Создание, обновление, удаление - только владельцу (owner==request.user)
'''


@extend_schema_view(
    list=extend_schema(
        summary='Список всех типов животных',
        description='Выводит список всех типов животных\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    retrieve=extend_schema(
        summary='Тип животного по ID',
        description='Возвращает конкретный тип животного по его `id`\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    create=extend_schema(
        summary='Создать новый тип животного',
        description='Добавляет новый тип животногоу\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    update=extend_schema(
        summary='Обновить тип животного',
        description='Полностью обновляет информацию о типе животного\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    partial_update=extend_schema(
        summary='Частично обновить тип животного',
        description='Обновляет отдельные поля типа животного\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    destroy=extend_schema(
        summary='Удалить тип животного',
        description='Удаляет тип животного по его `id`\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
)
class AnimalTypeViewSet(viewsets.ModelViewSet):
    queryset = AnimalType.objects.all()
    serializer_class = AnimalTypeSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdminOrReadOnly]


@extend_schema_view(
    list=extend_schema(
        summary='Список всех пород животных',
        description='Выводит список всех пород животных\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    retrieve=extend_schema(
        summary='Порода по ID',
        description='Возвращает конкретный породу по его `id`\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    create=extend_schema(
        summary='Создать новую породу животного',
        description='Добавляет новую породу животного\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    update=extend_schema(
        summary='Обновить породу животного',
        description='Полностью обновляет информацию о породе животного\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    partial_update=extend_schema(
        summary='Частично обновить породу животного',
        description='Обновляет отдельные поля породы животного\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    destroy=extend_schema(
        summary='Удалить породу животного',
        description='Удаляет породу животного по его `id`\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
)
class BreedViewSet(viewsets.ModelViewSet):
    queryset = Breed.objects.all()
    serializer_class = BreedSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdminOrReadOnly]


@extend_schema_view(
    list=extend_schema(
        summary='Список всех животных',
        description='Выводит список всех активных (is_active) животных\n\n' +
                    COMMON_OWNER_PERMISSION,
    ),
    retrieve=extend_schema(
        summary='Животное по ID',
        description='Возвращает конкретное животное по его `id`\n\n' +
                    COMMON_OWNER_PERMISSION,
    ),
    create=extend_schema(
        summary='Создать нового питомца',
        description='Создает нового питомца, владелец - пользователь, '
                    'отправивший запрос\n\n' + COMMON_OWNER_PERMISSION,
    ),
    update=extend_schema(
        summary='Обновить питомца',
        description='Полностью обновляет информацию о питомце\n\n' +
                    COMMON_OWNER_PERMISSION,
    ),
    partial_update=extend_schema(
        summary='Частично обновить питомца',
        description='Обновляет отдельные поля питомца\n\n' +
                    COMMON_OWNER_PERMISSION,
    ),
    destroy=extend_schema(
        summary='Удалить питомца',
        description='Удаляет питомца по его `id`\n\n' +
                    COMMON_OWNER_PERMISSION,
    ),
)
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

    @extend_schema(
            summary='Получить всех питомцев пользователя',
            description='Возвращает всех питомцев владельца, '
                        'отправившего запрос'
    )
    @action(detail=False, methods=['get'])
    def me(self, request):
        queryset = Pet.objects.filter(owner=request.user)
        serializer = PetSerializer(queryset, many=True)
        return Response(serializer.data)
