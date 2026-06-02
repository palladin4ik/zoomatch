from rest_framework import serializers

from pets.serializers_readonly import PetShortSerializer


class PetsCirclesResponseSerializer(serializers.Serializer):
    less_than_50 = PetShortSerializer(many=True)
    less_than_150 = PetShortSerializer(many=True)
    less_than_300 = PetShortSerializer(many=True)
    more_than_300 = PetShortSerializer(many=True)
