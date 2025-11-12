from rest_framework import serializers
from django.contrib.auth import get_user_model


User = get_user_model()


class UserCreateSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)

    class Meta:
        model = User
        fields = ('name', 'email', 'password')

    def create(self, validated_data):
        password = validated_data.pop('password')
        user = User.objects.create_user(password=password, **validated_data)
        return user


class UserSerializer(serializers.ModelSerializer):
    avatar = serializers.CharField(read_only=True)

    class Meta:
        model = User
        fields = ('id', 'name', 'email', 'avatar',
                  'location', 'phone_number', 'role', 'last_seen', 'is_active')
        read_only_fields = ('id', 'role', 'last_seen', 'is_active')


class UserUpdateSerializer(serializers.ModelSerializer):
    avatar = serializers.CharField()

    class Meta:
        model = User
        fields = ('id', 'name', 'email', 'avatar', 'location', 'phone_number')
        read_only_fields = ('id',)


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
