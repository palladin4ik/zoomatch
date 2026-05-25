import base64

from rest_framework import serializers
from django.contrib.auth import get_user_model

from pets.serializers_readonly import PetShortSerializer
from geo.services import build_location_from_input


User = get_user_model()


class Base64FileField(serializers.CharField):

    def to_internal_value(self, data):
        if not isinstance(data, str):
            raise serializers.ValidationError('Нужна строка Base64')

        try:
            decoded = base64.b64decode(data)
        except Exception:
            raise serializers.ValidationError('Неверный формат Base64')

        if len(decoded) > 10_000_000:
            raise serializers.ValidationError('Файл размером больше 10Мб')
        return data

    def to_representation(self, value):
        return value


class UserCreateSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)

    class Meta:
        model = User
        fields = ('firstname', 'lastname', 'email', 'password')

    def create(self, validated_data):
        password = validated_data.pop('password')

        user = User.objects.create_user(password=password, **validated_data)

        return user


class UserSerializer(serializers.ModelSerializer):
    avatar = Base64FileField(read_only=True)
    pets = PetShortSerializer(many=True, read_only=True)

    class Meta:
        model = User
        fields = ('id', 'firstname', 'lastname', 'email', 'avatar', 'location',
                  'description', 'phone_number', 'role', 'last_seen',
                  'organization', 'is_active', 'pets')
        read_only_fields = ('id', 'role', 'last_seen', 'is_active')


class SimpleUserSerializer(serializers.ModelSerializer):
    avatar = Base64FileField(read_only=True)

    class Meta:
        model = User
        fields = ('id', 'firstname', 'lastname', 'email', 'avatar', 'location',
                  'description', 'phone_number', 'role', 'last_seen',
                  'is_active')
        read_only_fields = ('id', 'role', 'last_seen', 'is_active')


class UserUpdateSerializer(serializers.ModelSerializer):
    avatar = Base64FileField(required=False, allow_null=True)

    address = serializers.CharField(write_only=True, required=False)
    latitude = serializers.FloatField(write_only=True, required=False)
    longitude = serializers.FloatField(write_only=True, required=False)

    class Meta:
        model = User
        fields = ('id', 'firstname', 'lastname', 'email', 'avatar', 'location',
                  'address', 'latitude', 'longitude', 'organization',
                  'phone_number', 'description')
        read_only_fields = ('id', 'location')

    def update(self, instance, validated_data):
        address = validated_data.pop('address', None)
        latitude = validated_data.pop('latitude', None)
        longitude = validated_data.pop('longitude', None)

        if address or latitude is not None or longitude is not None:
            validated_data['location'] = build_location_from_input(
                address, latitude, longitude
            )

        for attr, value in validated_data.items():
            setattr(instance, attr, value)

        instance.save()

        return instance


class ChangePasswordSerializer(serializers.Serializer):
    old_password = serializers.CharField(write_only=True, required=True)
    new_password = serializers.CharField(write_only=True, required=True)

    def validate_old_password(self, value):
        user = self.context['request'].user
        if not user.check_password(value):
            raise serializers.ValidationError('Неверный старый пароль')
        return value

    def update(self, instance, validated_data):
        instance.set_password(validated_data['new_password'])
        instance.save()
        return instance
