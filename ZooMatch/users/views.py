from rest_framework import mixins, viewsets, permissions, status
from rest_framework.response import Response
from rest_framework.decorators import action
from django.contrib.auth import get_user_model

from drf_spectacular.utils import (extend_schema, extend_schema_view,
                                   OpenApiResponse)

from .serializers import (
    UserCreateSerializer, UserSerializer,
    UserUpdateSerializer, ChangePasswordSerializer
)


User = get_user_model()


class RegistrationViewSet(mixins.CreateModelMixin, viewsets.GenericViewSet):
    queryset = User.objects.all()
    serializer_class = UserCreateSerializer
    permission_classes = [permissions.AllowAny]

    @extend_schema(
            summary='Регистрация пользователя',
            description='Регистрирует пользователя, права доступа AllowAny'
    )
    def create(self, request, *args, **kwargs):
        return super().create(request, *args, **kwargs)


# Пагинация питомцев у пользователя, переработать эндпоинты!!!!!!
class ProfileViewSet(viewsets.ViewSet):
    permission_classes = [permissions.IsAuthenticated]

    def get_serializer_class(self):
        if self.action == 'partial_update':
            return UserUpdateSerializer

        if self.action == 'change_password':
            return ChangePasswordSerializer

        return UserSerializer

    @extend_schema(
            summary='Профиль текущего пользователя',
    )
    def retrieve(self, request, pk=None):
        serializer = UserSerializer(request.user)
        return Response(serializer.data)

    @extend_schema(
            summary='Изменение профиля текущего пользователя',
    )
    def partial_update(self, request, pk=None):
        serializer = UserUpdateSerializer(request.user, data=request.data,
                                          partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(serializer.data)

    @extend_schema(
            summary='Удаление текущего пользователя',
    )
    def destroy(self, request, pk=None):
        request.user.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

    @extend_schema(
            summary='Смена пароля текущего пользователя',
            responses={
                200: OpenApiResponse(
                    response={
                        'type': 'object',
                        'properties': {
                            'detail': {
                                'type': 'string',
                                'example': 'Пароль успешно изменен',
                            }
                        }
                    }
                )
            }
    )
    @action(detail=False, methods=['patch'], url_path='change-password')
    def change_password(self, request):
        serializer = ChangePasswordSerializer(
            data=request.data, context={'request': request}
            )
        serializer.is_valid(raise_exception=True)
        serializer.update(request.user, serializer.validated_data)
        return Response({'detail': 'Пароль успешно изменен'})


@extend_schema_view(
    list=extend_schema(
        summary='Список всех пользователей',
        description='Доступно все авторизованным пользователям'
    ),
    retrieve=extend_schema(
        summary='Пользователь по id',
        description='Доступно все авторизованным пользователям'
    )
)
class UserViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = User.objects.all().prefetch_related('pets')
    serializer_class = UserSerializer
    permission_classes = [permissions.IsAuthenticated]
