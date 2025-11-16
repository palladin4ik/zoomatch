from rest_framework import serializers

from .models import Pet


class PetShortSerializer(serializers.ModelSerializer):

    class Meta:
        model = Pet
        fields = ('id', 'name', 'is_male', 'age', 'avatar', 'is_active')
