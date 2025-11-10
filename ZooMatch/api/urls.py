from rest_framework.routers import DefaultRouter
from django.urls import path, include
from rest_framework_simplejwt.views import (
    TokenObtainPairView,
    TokenVerifyView,
    TokenRefreshView
)
from drf_spectacular.views import (
    SpectacularAPIView, SpectacularSwaggerView,
    SpectacularRedocView
)

from users.views import RegistrationViewSet, ProfileViewSet, UserViewSet


router_v1 = DefaultRouter()
router_v1.register('register', RegistrationViewSet, basename='register')
router_v1.register('users', UserViewSet, basename='users')

profile = ProfileViewSet.as_view({
    'get': 'retrieve',
    'patch': 'partial_update',
    'delete': 'destroy',
})

change_password = ProfileViewSet.as_view({
    'patch': 'change_password'
})

urlpatterns = [
    path('v1/jwt/create/', TokenObtainPairView.as_view(), name='jwt_create'),
    path('v1/jwt/refresh/', TokenRefreshView.as_view(), name='jwt_refresh'),
    path('v1/jwt/verify/', TokenVerifyView.as_view(), name='jwt_verify'),
    path('v1/me/change-password/', change_password, name='change_password'),
    path('v1/me/', profile, name='profile'),
    path('v1/', include(router_v1.urls)),
    path('schema/', SpectacularAPIView.as_view(), name='schema'),
    path('docs/swagger/', SpectacularSwaggerView.as_view(url_name='schema'),
         name='swagger'),
    path('docs/redoc/', SpectacularRedocView.as_view(url_name='schema'),
         name='redoc'),
]
